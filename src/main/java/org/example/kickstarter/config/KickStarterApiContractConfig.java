package org.example.kickstarter.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "KickStarter API",
                version = "1.0.0",
                description = "REST API для платформы краудфандинга"
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local development")
        }
)
@SecurityScheme(
        name = KickStarterApiContractConfig.SECURITY_SCHEME_BEARER,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "JWT Bearer token. Пример: `Authorization: Bearer eyflhfhfH..."
)
public final class KickStarterApiContractConfig {

    public static final String SECURITY_SCHEME_BEARER = "bearerAuth";

    private KickStarterApiContractConfig() {
    }
}