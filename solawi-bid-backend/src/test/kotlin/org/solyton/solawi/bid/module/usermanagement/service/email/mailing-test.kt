package org.solyton.solawi.bid.module.usermanagement.service.email

import org.junit.jupiter.api.Test
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import org.solyton.solawi.bid.Api

class MailServiceTest {

    @Api
    @Test fun sendMail() {

        val mailer = MailerBuilder
            .withSMTPServer("smtp.strato.de", 587, "info@solyton.org", "xc~w?/E4-hl+4O")
            .withTransportStrategy(TransportStrategy.SMTP_TLS)
            .buildMailer()

        val email = EmailBuilder.startingBlank()
            .from("Solyton", "info@solyton.org")
            .to("Recipient Name", "mr.schmidt@gmx.net")
            .withSubject("Test Email")
            .withHTMLText("""
            <html>
              <body>
                <h1 style="color: navy;">Hello from Kotlin!</h1>
                <p>This is an <strong>HTML</strong> email.</p>
              </body>
            </html>
            """.trimIndent()
            )
            .buildEmail()
        mailer.sendMail(email)


    }

}
