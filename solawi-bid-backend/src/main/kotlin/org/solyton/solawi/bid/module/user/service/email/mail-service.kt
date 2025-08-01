package org.solyton.solawi.bid.module.user.service.email

import org.simplejavamail.api.email.Email
import org.simplejavamail.api.mailer.Mailer
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder

fun Mailer(
    host : String,
    port: Int,
    username: String,
    password: String,

): Mailer = MailerBuilder
    .withSMTPServer(
        host,
        port,
        username,
        password
    )
    .withTransportStrategy(TransportStrategy.SMTP_TLS)
    .buildMailer()

/*Ü
fun mailContent(content: TagConsumer<String>.)

val x = createHTML().html {
    body {
        h1 {
            +"Jakfldöa"
        }
    }

}

 */

fun createRegistrationMail(
    senderMail: String,
    recipientMail: String,
    registrationLink: String
): Email = EmailBuilder.startingBlank()
    .from(senderMail)
    .to(recipientMail)
    .withSubject("Registrierung Abschließen")
    .withHTMLText("""
            <html>
              <body>
                <h1 style="color: navy;">Herzlich willkommenbei Solyton</h1>
                <p>Um die Registrierung abzuschließen folgen Sie bitte diesem <a href="$registrationLink"> Link </a></p>
                
                <p>
              </body>
            </html>
            """.trimIndent()
    )
    .buildEmail()
