package com.example.farooq.firebaseblogapp.Post;


public class Post extends BlogPostId{

    public String description;
    public String user_id;
    public String image;
    public String thumb_image;
    public String timestamp;

    public Post() { }
    public Post(String description, String user_id, String image, String thumb_image, String timestamp) {
        this.description = description;
        this.user_id = user_id;
        this.image = image;
        this.thumb_image = thumb_image;
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
