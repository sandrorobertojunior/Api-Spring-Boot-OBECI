package org.obeci.platform.configs;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Interceptor do canal STOMP (mensagens inbound do cliente).
 *
 * <p>Função principal: garantir que a sessão STOMP tenha um "user" (Principal)
 * associado para que:
 * <ul>
 *   <li>o controller WS possa receber {@link java.security.Principal}</li>
 *   <li>o servidor possa enviar mensagens para /user/queue/**</li>
 * </ul>
 * </p>
 *
 * <p>Fonte do Authentication:
 * <ul>
 *   <li>salvo no handshake em {@link JwtHandshakeInterceptor#ATTR_AUTH}</li>
 * </ul>
 * </p>
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // No CONNECT, copiamos Authentication do handshake para o accessor.
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Authentication auth = resolveAuthFromSession(accessor);
            if (auth != null) {
                accessor.setUser(auth);
            }
        }

        return message;
    }

    @Nullable
    private Authentication resolveAuthFromSession(StompHeaderAccessor accessor) {
        if (accessor.getSessionAttributes() == null) {
            return null;
        }

        Object a = accessor.getSessionAttributes().get(JwtHandshakeInterceptor.ATTR_AUTH);
        if (a instanceof Authentication auth) {
            return auth;
        }
        return null;
    }
}
