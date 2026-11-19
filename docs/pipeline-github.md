# Pipeline de GitHub Actions

La canalización `.github/workflows/pipeline.yml` ejecuta tres etapas:

1. **Pruebas:** compila todos los módulos, ejecuta las pruebas, valida Docker Compose y comprueba que no se haya versionado `.env`.
2. **Paquete:** crea un `.tgz` únicamente con archivos rastreados por Git y publica su checksum SHA-256.
3. **Despliegue:** obtiene credenciales temporales de AWS mediante OIDC, almacena el paquete cifrado en S3 y ordena el despliegue a EC2 mediante Systems Manager. La instancia conserva su `.env` privado y revierte a la versión anterior si las comprobaciones fallan.

Los `pull_request` solo ejecutan pruebas. Los `push` a `main` y las ejecuciones manuales despliegan a `production`. El entorno de GitHub puede configurarse para exigir aprobación antes de desplegar.
Mientras `AWS_ROLE_ARN` no esté configurada, GitHub ejecuta las pruebas y genera el paquete, pero omite el despliegue; así la primera publicación del repositorio no produce un fallo artificial.

## 1. Publicar el repositorio

Este directorio todavía no tiene un remoto. Cree un repositorio privado o público en GitHub y conecte el proyecto:

```bash
git remote add origin https://github.com/PROPIETARIO/REPOSITORIO.git
git push -u origin main
```

## 2. Preparar AWS una sola vez

La EC2 debe aparecer como nodo administrado en Systems Manager. Las instancias nuevas creadas con `scripts/aws-provision.sh` ya reciben el perfil `grupo3-livebet-ec2-ssm`.

Cree el rol OIDC y el bucket de entregas con la plantilla incluida:

```bash
aws cloudformation deploy \
  --region us-east-2 \
  --stack-name grupo3-livebet-github \
  --template-file infrastructure/aws/github-actions-oidc.yml \
  --capabilities CAPABILITY_NAMED_IAM \
  --parameter-overrides \
    GitHubRepository=PROPIETARIO/REPOSITORIO \
    InstanceId=i-0e23ab3097f20bc1d \
    ArtifactBucketName=grupo3-livebet-deploy-ID_UNICO
```

Si la cuenta ya tiene configurado el proveedor OIDC `token.actions.githubusercontent.com`, añada `ExistingOidcProviderArn=ARN_EXISTENTE` a `--parameter-overrides`. La plantilla reutilizará ese proveedor en lugar de intentar duplicarlo.

La confianza OIDC está restringida al repositorio indicado y al entorno `production`. Esto coincide con el formato de identidad que GitHub emite cuando un trabajo utiliza un environment.

Para preparar la instancia ya existente y confirmar que aparezca en **Systems Manager → Fleet Manager**, ejecute:

```bash
AWS_REGION=us-east-2 ./scripts/aws-enable-ssm.sh i-0e23ab3097f20bc1d
```

## 3. Variables de GitHub

En **Settings → Environments → production**, agregue estas variables; no son secretos:

| Variable | Ejemplo |
|---|---|
| `AWS_ROLE_ARN` | Salida `AwsRoleArn` de CloudFormation |
| `AWS_REGION` | `us-east-2` |
| `DEPLOY_BUCKET` | Salida `DeployBucket` de CloudFormation |
| `EC2_INSTANCE_ID` | `i-0e23ab3097f20bc1d` |
| `DASHBOARD_URL` | `http://18.224.52.92:8080` |
| `API_URL` | `http://18.224.52.92:8081` |

No se necesitan `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, llaves SSH ni la contraseña de Gmail en GitHub.

## 4. Configuración privada del servidor

El archivo `/opt/livebet/.env` se configura una sola vez en EC2. Cada despliegue lo copia a la nueva versión sin imprimirlo en los registros. Debe contener las variables de PostgreSQL, RabbitMQ, JWT, administrador y correo SMTP.

## Recuperación

Cada entrega queda en `/opt/livebet/releases/<commit>`. Si las rutas de salud no responden, `scripts/deploy-release.sh` vuelve a levantar la versión anterior y GitHub marca el despliegue como fallido.
