#!/usr/bin/env bash
# Crea la EC2 aislada de desarrollo y registra su runner de GitHub Actions.
# Uso: GITHUB_RUNNER_TOKEN=... ./scripts/aws-provision-development.sh
set -euo pipefail

: "${GITHUB_RUNNER_TOKEN:?Falta el token temporal de registro del runner}"

REGION="${AWS_REGION:-us-east-2}"
REPOSITORY="${GITHUB_REPOSITORY_URL:-https://github.com/matheuruales/livebet-grupo3}"
INSTANCE_TYPE="${INSTANCE_TYPE:-m7i-flex.large}"
ENVIRONMENT="development"
NAME="livebet-${ENVIRONMENT}"

VPC_ID="$(aws ec2 describe-vpcs --region "$REGION" --filters Name=is-default,Values=true --query 'Vpcs[0].VpcId' --output text)"
SUBNET_ID="$(aws ec2 describe-subnets --region "$REGION" --filters Name=vpc-id,Values="$VPC_ID" --query 'Subnets[0].SubnetId' --output text)"
SG_ID="$(aws ec2 describe-security-groups --region "$REGION" --filters Name=vpc-id,Values="$VPC_ID" Name=group-name,Values="${NAME}-sg" --query 'SecurityGroups[0].GroupId' --output text)"
if [[ "$SG_ID" == "None" ]]; then
  SG_ID="$(aws ec2 create-security-group --region "$REGION" --vpc-id "$VPC_ID" --group-name "${NAME}-sg" --description 'HTTPS público de LiveBet desarrollo' --query GroupId --output text)"
fi
aws ec2 authorize-security-group-ingress --region "$REGION" --group-id "$SG_ID" --ip-permissions \
  'IpProtocol=tcp,FromPort=80,ToPort=80,IpRanges=[{CidrIp=0.0.0.0/0,Description="HTTP redirect"}]' \
  'IpProtocol=tcp,FromPort=443,ToPort=443,IpRanges=[{CidrIp=0.0.0.0/0,Description="HTTPS LiveBet desarrollo"}]' 2>/dev/null || true

ROLE_NAME="livebet-development-ssm"
PROFILE_NAME="livebet-development-ssm"
if ! aws iam get-role --role-name "$ROLE_NAME" >/dev/null 2>&1; then
  aws iam create-role --role-name "$ROLE_NAME" --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ec2.amazonaws.com"},"Action":"sts:AssumeRole"}]}' >/dev/null
fi
aws iam attach-role-policy --role-name "$ROLE_NAME" --policy-arn arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore
if ! aws iam get-instance-profile --instance-profile-name "$PROFILE_NAME" >/dev/null 2>&1; then
  aws iam create-instance-profile --instance-profile-name "$PROFILE_NAME" >/dev/null
  aws iam add-role-to-instance-profile --instance-profile-name "$PROFILE_NAME" --role-name "$ROLE_NAME"
  sleep 10
fi

USER_DATA="$(mktemp)"
cat > "$USER_DATA" <<EOF
#!/usr/bin/env bash
set -euxo pipefail
apt-get update
apt-get install -y ca-certificates curl tar git python3
curl -fsSL https://get.docker.com | sh
useradd --create-home --shell /bin/bash actions || true
usermod -aG docker actions
printf 'actions ALL=(ALL) NOPASSWD: ALL\n' >/etc/sudoers.d/actions
chmod 440 /etc/sudoers.d/actions
mkdir -p /opt/actions-runner
RUNNER_URL=\$(python3 - <<'PY'
import json, urllib.request
data = json.load(urllib.request.urlopen('https://api.github.com/repos/actions/runner/releases/latest'))
print(next(asset['browser_download_url'] for asset in data['assets'] if asset['name'].startswith('actions-runner-linux-x64-') and asset['name'].endswith('.tar.gz')))
PY
)
curl -fL "\$RUNNER_URL" -o /tmp/actions-runner.tar.gz
tar -xzf /tmp/actions-runner.tar.gz -C /opt/actions-runner
cd /opt/actions-runner
./bin/installdependencies.sh || true
chown -R actions:actions /opt/actions-runner
su - actions -c 'cd /opt/actions-runner && ./config.sh --unattended --url "${REPOSITORY}" --token "${GITHUB_RUNNER_TOKEN}" --name "${NAME}" --labels self-hosted,linux,x64,livebet-development --work _work'
cd /opt/actions-runner
./svc.sh install actions
./svc.sh start
EOF

AMI_ID="$(aws ssm get-parameter --region "$REGION" --name /aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id --query Parameter.Value --output text)"
INSTANCE_ID="$(aws ec2 run-instances --region "$REGION" --image-id "$AMI_ID" --instance-type "$INSTANCE_TYPE" \
  --iam-instance-profile "Name=$PROFILE_NAME" \
  --network-interfaces "AssociatePublicIpAddress=true,DeviceIndex=0,Groups=$SG_ID,SubnetId=$SUBNET_ID" \
  --block-device-mappings 'DeviceName=/dev/sda1,Ebs={VolumeSize=40,VolumeType=gp3,DeleteOnTermination=true}' \
  --metadata-options HttpTokens=required,HttpEndpoint=enabled \
  --user-data "file://$USER_DATA" \
  --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$NAME},{Key=Environment,Value=development}]" \
  --query 'Instances[0].InstanceId' --output text)"
rm -f "$USER_DATA"
aws ec2 wait instance-running --region "$REGION" --instance-ids "$INSTANCE_ID"
PUBLIC_IP="$(aws ec2 describe-instances --region "$REGION" --instance-ids "$INSTANCE_ID" --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)"
printf 'INSTANCE_ID=%s\nPUBLIC_IP=%s\nDOMAIN=livebet.%s.nip.io\n' "$INSTANCE_ID" "$PUBLIC_IP" "$PUBLIC_IP"
