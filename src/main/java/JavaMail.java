import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class JavaMail {

    protected Properties envProp;

    public static void main(String[] args) throws IOException {
        JavaMail mail = new JavaMail();
        mail.program();
    }

    public void program() throws IOException {
        EmailContent content = new EmailContent();
        this.envProp = new Properties();

        //Get data from file to the properties variable
        FileInputStream input = new FileInputStream("src/main/java/env.properties");

        //Load the data into the properties file
        this.envProp.load(input);

        //Add subject to email
        content.subject = this.envProp.getProperty("EMAIL_SUBJECT");

        //Use this to send emails with other content
        content.content = this.envProp.getProperty("EMAIL_CONTENT");
        sendEmail(this.envProp.getProperty("EMAIL"), content);

        //Use this to send emails with content that will be found
        content.content = this.envProp.getProperty("EMAIL_CONTENT_TO_FIND");
        sendEmail(this.envProp.getProperty("EMAIL"), content);

        //Use this to find emails and print them to the console
        getEmail(this.envProp.getProperty("EMAIL"), "find");
    }

    public void getEmail(String from, String toFindStr) {
        final String username = from;
        final String appPassword = this.envProp.getProperty("APP_PASS");
        final String host = this.envProp.getProperty("HOST_IN");

        try {
            Session emailSession = createSession(host);

            //create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("pop3s");

            store.connect(host, username, appPassword);

            //create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            //search and retrieve the correct messages from the folder and print it
            Message[] messages = emailFolder.getMessages();
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                if (message.getContent().toString().contains(toFindStr)) {
                    System.out.println("Email number " + (i + 1) + ", is a correct email to find");
                    System.out.println("---------------------------------");
                    System.out.println("From: " + message.getFrom()[0]);
                    System.out.println("Subject: " + message.getSubject());
                    System.out.println("Content: " + message.getContent().toString());
                }
            }

            //close the store and folder objects
            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendEmail(String to, EmailContent EmailContent) {
        final String host = this.envProp.getProperty("HOST_OUT");

        try {
            Session emailSession = createSession(host);

            // Create a default MimeMessage object.
            Message message = new MimeMessage(emailSession);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(this.envProp.getProperty("EMAIL")));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            // Set Subject: header field
            message.setSubject(EmailContent.subject);

            // Now set the actual message
            message.setText(EmailContent.content);

            // Send message
            Transport.send(message);

            System.out.println("The Message Sent successfully");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    static class EmailContent {
        String subject;
        String content;
    }

    public Session createSession(String host) {
        final String username = this.envProp.getProperty("USER_NAME");
        final String appPassword = this.envProp.getProperty("APP_PASS");
        Session emailSession;
        Properties props = new Properties();

        if (host.contains("pop")) {
            //create properties field
            props.put("mail.pop3.host", host);
            props.put("mail.pop3.port", "995");
            props.put("mail.pop3.starttls.enable", "true");
            emailSession = Session.getDefaultInstance(props);
        } else {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.starttls.enable", "true");
            emailSession = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, appPassword);
                        }
                    });
        }
        return emailSession;
    }
}
