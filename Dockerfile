# Use the official Keycloak image as the base
FROM docker.io/amphan613/amphan:latest

# Set environment variables
ENV KEYCLOAK_USER=admin
ENV KEYCLOAK_PASSWORD=admin

# Expose the Keycloak port
EXPOSE 8080

# Run Keycloak with import option
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm"]