# Despliegue en AWS EC2

El despliegue académico utiliza una sola instancia EC2 con Docker Compose. Mantiene la misma arquitectura demostrada localmente y evita introducir servicios administrados que no exige el ejercicio.

## Recursos recomendados

- Ubuntu Server 24.04 LTS sobre arquitectura x86_64.
- 4 vCPU y 16 GiB de RAM como referencia segura para toda la plataforma.
- Volumen gp3 de 40 GiB.
- IP pública elástica si la demostración requiere una URL estable.

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
