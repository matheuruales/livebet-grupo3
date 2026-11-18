# Despliegue en AWS EC2

El despliegue académico utiliza una sola instancia EC2 con Docker Compose. Mantiene la misma arquitectura demostrada localmente y evita introducir servicios administrados que no exige el ejercicio.

## Recursos recomendados

- Ubuntu Server 24.04 LTS sobre arquitectura x86_64.
- 4 vCPU y 16 GiB de RAM como referencia segura para toda la plataforma.
- Volumen gp3 de 40 GiB.
- IP pública elástica si la demostración requiere una URL estable.

## Despliegue realizado para la entrega

El 18 de noviembre de 2026 se aprovisionó y verificó una instancia EC2 `m7i-flex.large` en `us-east-2`, con Ubuntu 24.04, volumen gp3 de 40 GiB y Docker Compose. La cuenta académica restringe los tipos de instancia disponibles; por ello se utilizó este tipo en lugar de `t3.xlarge`.

- Dashboard: <http://18.189.178.18:8080>
- API: <http://18.189.178.18:8081>
- Instancia: `i-02a7ef9138c943273`

La prueba integral en AWS registró la apuesta `b-cdb8f691` por 20.000 COP en el partido `m-1c7fafee`. El partido terminó con ganador Nacional y el dashboard confirmó una apuesta y un total de 20.000 COP.

La IP es pública pero no elástica: cambiará si la instancia se detiene y vuelve a iniciar. Detener o terminar la instancia después de la exposición evita cargos.

## Grupo de seguridad

| Puerto | Origen | Uso |
|---:|---|---|
| 22 | IP del equipo | Administración SSH |
| 8080 | IP de la universidad o temporalmente público | Dashboard |
| 8081 | IP de la universidad o temporalmente público | API de apuestas |
| 15672 | Solo IP del equipo y únicamente durante la demo | RabbitMQ Management |

No exponer 5432, 5672, 9092 ni 2181.

## Instalación

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl git
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
```

Cerrar la sesión SSH y volver a entrar para aplicar el grupo `docker`.

## Publicación

El script reproducible `scripts/aws-provision.sh` automatiza la creación del grupo de seguridad, el empaquetado, la instalación de Docker y el arranque. También admite cambiar el tipo de instancia mediante `INSTANCE_TYPE`.

```bash
AWS_REGION=us-east-2 INSTANCE_TYPE=m7i-flex.large ./scripts/aws-provision.sh
```

Como alternativa manual:

```bash
git clone URL_DEL_REPOSITORIO live-betting
cd live-betting
cp .env.example .env
nano .env
docker compose up --build -d
docker compose ps
```

Cambiar los secretos de RabbitMQ y PostgreSQL en `.env`. El dashboard quedará disponible en `http://IP_PUBLICA:8080`.

## Operación

```bash
docker compose logs -f --tail=100
docker compose pull
docker compose up --build -d
docker compose down
```

Detener la instancia al terminar la evaluación evita costos innecesarios. El volumen EBS y una IP elástica sin instancia pueden seguir generando cargos; eliminarlos cuando ya no se necesiten.
