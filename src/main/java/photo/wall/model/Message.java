package photo.wall.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "MESSAGES")
public class Message {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "TIMESTAMP")
    private Date timestamp;

    Message() {
        this.timestamp = new Date();
    }

    public Message(String message) {
        this();
        this.message = message;
    }

    public static Message emptyMessage() {
        return new Message(StringUtils.EMPTY);
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }

        Message message = (Message) o;

        return id != null ? id.equals(message.id) : message.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
