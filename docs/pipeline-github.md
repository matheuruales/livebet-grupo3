# Pipeline de GitHub Actions

La canalización `.github/workflows/pipeline.yml` está conectada al repositorio privado `matheuruales/livebet-grupo3` y a la instancia AWS EC2 de producción.

## Funcionamiento

1. **Pruebas:** compila todos los módulos, ejecuta las pruebas, valida Docker Compose y comprueba que `.env` no esté versionado.
2. **Paquete:** crea un `.tgz` únicamente con archivos rastreados por Git y calcula su checksum SHA-256.
3. **Despliegue:** el ejecutor privado `livebet-aws-production`, instalado como servicio dentro de la EC2, descarga el paquete aprobado y lo instala en `/opt/livebet/releases/<commit>`.
4. **Verificación:** GitHub comprueba la salud del dashboard y la API. Si la versión nueva no responde, el script vuelve a levantar la anterior.

Los `pull_request` solo ejecutan pruebas. Cada `push` a `main` y cada ejecución manual despliegan automáticamente a `production`.

## Configuración ya creada

El entorno de GitHub `production` contiene:

| Tipo | Nombre | Uso |
|---|---|---|
| Variable | `DASHBOARD_URL` | Salud y enlace del sitio público |
| Variable | `API_URL` | Salud de la API pública |
| Secreto cifrado | `PRODUCTION_ENV` | PostgreSQL, RabbitMQ, JWT, administrador y Gmail SMTP |

El `.env` real nunca se agrega al repositorio ni se imprime en los registros. Durante el despliegue se crea con permisos privados, se copia a la nueva versión y se elimina la copia temporal incluso si el trabajo falla.

## Ejecutor privado

El ejecutor está registrado con las etiquetas `self-hosted`, `Linux`, `X64` y `livebet-production`. En la EC2 funciona como el servicio:

```text
actions.runner.matheuruales-livebet-grupo3.livebet-aws-production.service
```

No necesita llaves AWS ni una llave SSH guardada en GitHub. La cuenta AWS utilizada por el proyecto restringe mediante una política de organización la creación de roles OIDC; por eso el ejecutor reside directamente en la instancia y realiza el despliegue local.

## Actualizar variables privadas

Si cambia una contraseña o configuración, actualice el secreto completo `PRODUCTION_ENV` en **GitHub → Settings → Environments → production**. Debe conservar el formato `CLAVE=valor`, una variable por línea.

## Recuperación

Cada entrega queda en `/opt/livebet/releases/<commit>` y el enlace `/opt/livebet/current` señala la versión activa. `scripts/deploy-release.sh` valida el paquete, levanta los contenedores, comprueba dashboard y API y ejecuta la reversión automática si algo falla.

El historial y los saldos permanecen en los volúmenes de Docker; publicar una nueva versión no recrea esos datos.
