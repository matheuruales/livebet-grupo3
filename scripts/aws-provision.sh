#!/usr/bin/env bash
set -euo pipefail

REGION="${AWS_REGION:-us-east-2}"
ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
ARCHIVE="${1:-livebet-grupo3.tgz}"
BUCKET="grupo3-livebet-${ACCOUNT_ID}-${REGION}"

aws s3api head-bucket --bucket "$BUCKET" 2>/dev/null || aws s3 mb "s3://$BUCKET" --region "$REGION"
aws s3 cp "$ARCHIVE" "s3://$BUCKET/livebet-grupo3.tgz" --region "$REGION"
PRESIGNED="$(aws s3 presign "s3://$BUCKET/livebet-grupo3.tgz" --expires-in 3600 --region "$REGION")"

VPC_ID="$(aws ec2 describe-vpcs --region "$REGION" --filters Name=is-default,Values=true --query 'Vpcs[0].VpcId' --output text)"
SUBNET_ID="$(aws ec2 describe-subnets --region "$REGION" --filters Name=vpc-id,Values="$VPC_ID" --query 'Subnets[0].SubnetId' --output text)"
SG_ID="$(aws ec2 describe-security-groups --region "$REGION" --filters Name=vpc-id,Values="$VPC_ID" Name=group-name,Values=grupo3-livebet-sg --query 'SecurityGroups[0].GroupId' --output text)"
if [[ "$SG_ID" == "None" ]]; then
  SG_ID="$(aws ec2 create-security-group --region "$REGION" --vpc-id "$VPC_ID" --group-name grupo3-livebet-sg --description 'Dashboard y API Grupo 3' --query GroupId --output text)"
fi
aws ec2 authorize-security-group-ingress --region "$REGION" --group-id "$SG_ID" --ip-permissions \
  'IpProtocol=tcp,FromPort=8080,ToPort=8080,IpRanges=[{CidrIp=0.0.0.0/0,Description="Dashboard LiveBet"}]' \
  'IpProtocol=tcp,FromPort=8081,ToPort=8081,IpRanges=[{CidrIp=0.0.0.0/0,Description="API LiveBet"}]' 2>/dev/null || true

POSTGRES_PASSWORD="$(openssl rand -hex 16)"
RABBITMQ_PASSWORD="$(openssl rand -hex 16)"
cat > /tmp/livebet-user-data.sh <<EOF
#!/bin/bash
set -euxo pipefail
exec > /var/log/livebet-bootstrap.log 2>&1
apt-get update
apt-get install -y ca-certificates curl
curl -fsSL https://get.docker.com | sh
mkdir -p /opt/livebet
curl -fL '$PRESIGNED' -o /tmp/livebet-grupo3.tgz
tar -xzf /tmp/livebet-grupo3.tgz -C /opt/livebet
cat > /opt/livebet/.env <<ENV
POSTGRES_USER=betting
POSTGRES_PASSWORD=$POSTGRES_PASSWORD
RABBITMQ_USER=betting
RABBITMQ_PASSWORD=$RABBITMQ_PASSWORD
MATCH_DURATION_MS=60000
GENERATION_INTERVAL_MS=10000
ENV
cd /opt/livebet
docker compose up --build -d
EOF

AMI_ID="$(aws ssm get-parameter --region "$REGION" --name /aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id --query Parameter.Value --output text)"
INSTANCE_ID="$(aws ec2 run-instances --region "$REGION" --image-id "$AMI_ID" --instance-type t3.xlarge \
  --network-interfaces "AssociatePublicIpAddress=true,DeviceIndex=0,Groups=$SG_ID,SubnetId=$SUBNET_ID" \
  --block-device-mappings 'DeviceName=/dev/sda1,Ebs={VolumeSize=40,VolumeType=gp3,DeleteOnTermination=true}' \
  --metadata-options HttpTokens=required,HttpEndpoint=enabled \
  --user-data file:///tmp/livebet-user-data.sh \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=grupo3-livebet}]' \
  --query 'Instances[0].InstanceId' --output text)"

aws ec2 wait instance-running --region "$REGION" --instance-ids "$INSTANCE_ID"
PUBLIC_IP="$(aws ec2 describe-instances --region "$REGION" --instance-ids "$INSTANCE_ID" --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)"
printf 'INSTANCE_ID=%s\nPUBLIC_IP=%s\nDASHBOARD=http://%s:8080\nAPI=http://%s:8081\n' "$INSTANCE_ID" "$PUBLIC_IP" "$PUBLIC_IP" "$PUBLIC_IP"
