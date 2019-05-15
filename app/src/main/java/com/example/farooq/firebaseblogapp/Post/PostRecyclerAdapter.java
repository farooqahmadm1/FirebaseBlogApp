package com.example.farooq.firebaseblogapp.Post;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.farooq.firebaseblogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.constraint.Constraints.TAG;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.PostViewHolder> {

    private FirebaseUser mAuth;
    private FirebaseFirestore firestore;
    private List<Post> postList;

    public PostRecyclerAdapter(List<Post> postList) {
        this.postList = postList;
    }
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.single_post_layout,parent,false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder holder, int i) {

        holder.setIsRecyclable(false);
        final String blogPostId = postList.get(i).BlogPostId;
        holder.setDescView(postList.get(i).getDescription());
        holder.setImageView(postList.get(i).getThumb_image());
        holder.setDateView(postList.get(i).getTimestamp());

        final String user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore= FirebaseFirestore.getInstance();
        firestore.collection("Users").document(postList.get(i).getUser_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    holder.setProfileImage(task.getResult().getString("image"));
                    holder.setNameView(task.getResult().getString("name"));
                }else {
                    Log.d(TAG, "onComplete:User Data Loading Error in post");
                }
            }
        });
        firestore.collection("Posts").document(blogPostId)
                .collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                int i=0;
                if (!documentSnapshots.isEmpty()){
                    for (DocumentChange documnet: documentSnapshots.getDocumentChanges()){
                        if (documnet.getType() == DocumentChange.Type.ADDED){
                            i++;
                        }
                        if (documnet.getType() == DocumentChange.Type.REMOVED){
                            i--;
                        }
                    }
                    holder.likeCountView.setText(i+" Likes");
                }
                holder.likeCountView.setText(i+" Likes");
            }
        });
        firestore.collection("Posts").document(blogPostId)
                .collection("Likes").document(user_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()){
                    holder.likeView.setImageResource(R.drawable.action_like_red);
                }else {
                    holder.likeView.setImageResource(R.drawable.action_like);
                }
            }
        });
        holder.likeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firestore.collection("Posts").document(blogPostId)
                        .collection("Likes").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()){
                            Map map = new HashMap();
                            map.put("timestamp", new SimpleDateFormat("dd/MM/yyyy").format(new Date())+"");
                            firestore.collection("Posts").document(blogPostId).collection("Likes").document(user_id).set(map);
                        }else {
                            firestore.collection("Posts").document(blogPostId).collection("Likes").document(user_id).delete();
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView descView;
        private TextView nameView;
        private TextView dateView;
        private ImageView imageView;
        private CircleImageView profileImage;
        public ImageView likeView;
        public TextView likeCountView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            descView = (TextView)  itemView.findViewById(R.id.post_description);
            nameView = (TextView) itemView.findViewById(R.id.post_username);
            dateView = (TextView) itemView.findViewById(R.id.post_date);
            imageView = (ImageView) itemView.findViewById(R.id.post_image);
            profileImage = (CircleImageView) itemView.findViewById(R.id.post_profile);
            likeView = (ImageView) itemView.findViewById(R.id.post_like_btn);
            likeCountView = (TextView) itemView.findViewById(R.id.post_like_count);
        }
        public void setDescView(String desc){
            descView.setText(desc);
        }
        public void setNameView(String name){
            nameView.setText(name);;
        }
        public void setDateView(String date){
            dateView.setText(date);;
        }
        public void setProfileImage(String profilelink) {
            Picasso.get().load(profilelink).placeholder(R.drawable.profile).into(profileImage);
        }
        public void setImageView(String ImageLink) {
            Picasso.get().load(ImageLink).placeholder(R.drawable.profile).into(imageView);
        }
    }
}