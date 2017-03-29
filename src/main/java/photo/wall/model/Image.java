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
@Table(name = "IMAGES")
public class Image {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "IMAGE_NAME")
    private String name;

    @Column(name = "IMAGE_URL")
    private String url;

    @Column(name = "TIMESTAMP")
    private Date timestamp;

    @Column(name = "WIDTH")
    private Integer width;

    @Column(name = "HEIGHT")
    private Integer height;

    @Column(name = "MESSAGE")
    private String message = StringUtils.EMPTY;

    Image() {
        this.timestamp = new Date();
    }

    public Image(String name, String url, Integer width, Integer height) {
        this.name = name;
        this.url = url;
        this.width = width;
        this.height = height;
        this.message = StringUtils.EMPTY;
    }

    public static Image emptyImage() {
        return new Image(StringUtils.EMPTY, StringUtils.EMPTY, 0, 0);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Image)) {
            return false;
        }

        Image image = (Image) o;

        return id != null ? id.equals(image.id) : image.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", timestamp=" + timestamp +
                ", width=" + width +
                ", height=" + height +
                ", message='" + message + '\'' +
                '}';
    }
}
