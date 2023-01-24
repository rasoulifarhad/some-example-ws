package com.example.democloudstreamwebfux.intergration.mail;

/**
 * Email
 * 
 * The process of sending email from within an application can be divided in two logical steps: 
 *   (1) preparing an email message by populating its subject, body, attachments, and recipients list and 
 *   (2) the operation of sending it. 
 * 
 *  Email-producing endpoint (Notification) -> header Enricher -> POJO Transformer ->  (Email) OutboundEmail Channel Adapter -> Smtp Server
 * 
 * The operation of sending email messages from your application is fairly straightforward and intuitive, considering 
 * that it’s based on a pattern you’re already familiar with: the outbound channel adapter. 
 * 
 * As a first step, you must configure such an adapter, which can be as simple as where the username and password attributes describe 
 * the login credentials for using the Simple Mail Transfer Protocol ( SMTP)  services provided by the host
 * 
 * What you send in a message can be, in the simplest case, equivalent to the following snippet; the outbound channel adapter 
 * composes a message with the payload as the body and the recipients lists (to, cc, bcc) and subject as header values
 * 
 * mail messages are usually produced by upstream endpoints or by a publisher interceptor. The end result, regardless of what led 
 * to it, is that a Spring Integration message with a mail-specific payload and mail-specific headers is sent to a channel to 
 * which an outbound email channel adapter listens, and then composes an email message based on payload and header content 
 * and sends it.
 * 
 * Sending mail
 *  
 *   Spring Integration mail ----> Spring Framework mail support  ---->  JavaMail
 * 
 *   Outbound endpoint -----> JavaMailSender ---> JavaMail API 
 *                  ^          ^          ^        ^
 *                  |          |          |        |
 *                  MailMessage          MimeMessage
 * 
 * Sending email is usually part of a larger chain of events, and to illustrate that, let’s consider a business example. 
 * As soon as the application is notified of a flight schedule change, it must inform all the customers who are booked on that flight. The 
 * upstream service activator takes care of searching for accounts  and producing one or more Notification objects.
 * 
 *    
 *   <chain input-channel="notificationRequest" output-channel="outboundMail">
 *      <service-activator ref="notificationService"/>
 *      <mail:header-enricher>
 *         <mail:to expression="payload.email"/>
 *         <mail:from value="${settings.email.from}"/>
 *         <mail:subject value="Notification"/>
 *      </mail:header-enricher>
 *      <transformer ref="templateMessageGenerator"/>
 *   </chain> 
 * 
 * Receiving email
 * 
 * The inbound channel adapter creates messages with a JavaMail MimeMessage payload, and the messages are sent on the adapted channel for 
 * further processing downstream. For separating concerns in the application, the MimeMessages are converted to domain objects 
 * downstream, typically using a transformer.
 * 
 * The first step for creating such an application is deciding how to receive emails what kind of a channel adapter you’d like to 
 * include. You can either poll the mailbox or be notified when new messages arrive,
 * 
 *  (Email Server)Mailbox -> (Channel Adapter)Inbound email -> (Email channel)MimeMessage -> Transformer -> (Request Channel)Domain object
 * 
 * Polling for emails
 * 
 * Polling is the most basic email-receiving strategy, and it’s supported by all mail servers. A client connects to the mailbox 
 * and downloads any unread messages that have arrived since the last polling cycle.
 * 
 * Event-driven email reception
 * 
 * Transforming inbound messages
 * 
 * Once you configure your inbound channel adapter, the next step is to process the received messages downstream. For handling the 
 * lower-level JavaMail Message objects in the domain-aware services, it’s typical to include a transformer before the objects 
 * enter the application flow.
 * 
 *  <chain input-channel="emails" output-channel="handled-emails">
 *     <transformer expression="payload.content"/>
 *     <service-activator ref="emailHandlingService"/>
 *  </chain>
 * 
 *  <chain input-channel="emails" output-channel="handled-emails">
 *     <mail:mail-to-string-transformer />
 *     <service-activator ref="emailHandlingService"/>
 *  </chain>
 * 
 * 
 */

public class Application {
    
}
