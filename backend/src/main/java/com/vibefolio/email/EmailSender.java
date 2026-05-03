package com.vibefolio.email;

/**
 * Abstraction for transactional email sending (implemented via Resend).
 * Services depend on this interface — never on the concrete Resend implementation.
 */
public interface EmailSender {

    /**
     * Send a magic link email to the given recipient.
     *
     * @param to           recipient email address
     * @param magicLinkUrl full magic link URL (token embedded)
     */
    void sendMagicLink(String to, String magicLinkUrl);
}
