# Centro de documentación de LiveBet

Este directorio contiene la documentación técnica, funcional y operativa de **LiveBet**, proyecto 8 de Computación Ubicua del Grupo 3 de Sexto B.

## Ruta de lectura recomendada

1. [Documentación técnica completa](documentacion-tecnica-completa.md): alcance, principios, componentes, flujos, consistencia, resiliencia y decisiones.
2. [Catálogo de diagramas](diagramas.md): arquitectura, clases, datos, secuencias, despliegue, redes y CI/CD.
3. [Referencia de API y contratos](referencia-api-y-eventos.md): endpoints HTTP, autenticación, validaciones, eventos Kafka y mensajes RabbitMQ.
4. [Integración de Mercado Pago](mercado-pago.md): Checkout Pro, webhooks firmados, variables y pruebas.
4. [Modelo de datos](modelo-de-datos.md): tablas, propiedad de datos, estados, relaciones e idempotencia.
5. [Operación, despliegue y seguridad](operacion-despliegue-seguridad.md): ejecución local, AWS, pipeline, recuperación, observabilidad y riesgos.
6. [Estrategia de pruebas y trazabilidad](pruebas-y-trazabilidad.md): matriz requisito-prueba, comandos, cobertura funcional y criterios de aceptación.
7. [Manual de usuario y administrador](manual-de-usuario.md): uso de la plataforma desde la interfaz.
8. [Guía de demostración](guia-demostracion.md): guion de exposición de 15 minutos.

## Documentos breves existentes

- [Arquitectura y responsabilidades del código](arquitectura-del-codigo.md)
- [Decisiones técnicas](decisiones-tecnicas.md)
- [Modelo y pruebas](modelo-y-pruebas.md)
- [Despliegue en AWS](despliegue-aws.md)
- [Pipeline de GitHub Actions](pipeline-github.md)

## Convenciones

- Los diagramas utilizan Mermaid y se renderizan directamente en GitHub.
- Los nombres de topics, exchanges, colas, routing keys y tablas coinciden con el código.
- Las credenciales reales no forman parte de la documentación ni del repositorio.
- Los ejemplos de tarjetas, correos, tokens e identificadores son ficticios.

## Versionado de la documentación

La documentación describe la versión desplegada desde la rama `main`. Cuando cambie un contrato HTTP, evento, tabla o variable de entorno, se debe actualizar el documento correspondiente en el mismo pull request.
