# custom-claim-spi
project which add custom claim to KeyCloak JWT Token

Running KeyCloak with Podman
Build: podman build -t spi-keycloak:latest .
Run: podman run -d --name spi-keycloak -p 8080:8080 spi-keycloak:latest