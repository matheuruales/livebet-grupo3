#!/usr/bin/env bash
set -euo pipefail

INSTANCE_ID="${1:-}"
REGION="${AWS_REGION:-us-east-2}"
ROLE_NAME="grupo3-livebet-ec2-ssm"
PROFILE_NAME="grupo3-livebet-ec2-ssm"

if [[ ! "$INSTANCE_ID" =~ ^i-[a-f0-9]+$ ]]; then
  printf 'Uso: AWS_REGION=us-east-2 %s i-xxxxxxxxxxxxxxxxx\n' "$0" >&2
  exit 2
fi

if ! aws iam get-role --role-name "$ROLE_NAME" >/dev/null 2>&1; then
  TRUST_POLICY="$(mktemp)"
  printf '%s' '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ec2.amazonaws.com"},"Action":"sts:AssumeRole"}]}' > "$TRUST_POLICY"
  aws iam create-role --role-name "$ROLE_NAME" \
    --assume-role-policy-document "file://$TRUST_POLICY" >/dev/null
fi
aws iam attach-role-policy --role-name "$ROLE_NAME" \
  --policy-arn arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore

if ! aws iam get-instance-profile --instance-profile-name "$PROFILE_NAME" >/dev/null 2>&1; then
  aws iam create-instance-profile --instance-profile-name "$PROFILE_NAME" >/dev/null
  aws iam add-role-to-instance-profile --instance-profile-name "$PROFILE_NAME" --role-name "$ROLE_NAME"
  sleep 10
fi

ASSOCIATION_ID="$(aws ec2 describe-iam-instance-profile-associations \
  --region "$REGION" \
  --filters "Name=instance-id,Values=$INSTANCE_ID" "Name=state,Values=associating,associated" \
  --query 'IamInstanceProfileAssociations[0].AssociationId' --output text)"
if [[ "$ASSOCIATION_ID" == "None" ]]; then
  aws ec2 associate-iam-instance-profile --region "$REGION" --instance-id "$INSTANCE_ID" \
    --iam-instance-profile "Name=$PROFILE_NAME" >/dev/null
else
  PROFILE_ARN="$(aws ec2 describe-iam-instance-profile-associations \
    --region "$REGION" --association-ids "$ASSOCIATION_ID" \
    --query 'IamInstanceProfileAssociations[0].IamInstanceProfile.Arn' --output text)"
  if [[ "$PROFILE_ARN" != */"$PROFILE_NAME" ]]; then
    printf 'La instancia ya tiene otro perfil IAM (%s); no se reemplazó automáticamente\n' "$PROFILE_ARN" >&2
    exit 1
  fi
fi

printf 'Perfil %s asociado a %s. Esperando registro en Systems Manager...\n' "$PROFILE_NAME" "$INSTANCE_ID"
attempts=0
while (( attempts < 36 )); do
  ((attempts += 1))
  PING_STATUS="$(aws ssm describe-instance-information --region "$REGION" \
    --filters "Key=InstanceIds,Values=$INSTANCE_ID" \
    --query 'InstanceInformationList[0].PingStatus' --output text)"
  if [[ "$PING_STATUS" == "Online" ]]; then
    printf 'La instancia está Online en Systems Manager\n'
    exit 0
  fi
  sleep 5
done

printf 'El perfil quedó asociado, pero el agente SSM aún no aparece Online\n' >&2
exit 1
