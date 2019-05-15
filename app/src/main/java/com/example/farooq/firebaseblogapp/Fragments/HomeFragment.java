package com.example.farooq.firebaseblogapp.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.farooq.firebaseblogapp.Post.Post;
import com.example.farooq.firebaseblogapp.Post.PostRecyclerAdapter;
import com.example.farooq.firebaseblogapp.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static Context context1;
    private static Fragment instance= null;
    public static Fragment getInstance(Context context){
        if (instance==null){
            instance= new HomeFragment();
        }
        context1 = context;
        return instance;
    }

    private RecyclerView recyclerView;
    private List<Post> postList;
    private PostRecyclerAdapter adapter;

    //firebase
    private FirebaseFirestore firestore;
    private DocumentSnapshot lastVisible;
    private Boolean isPageFirstTimeLoaded = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        postList =  new ArrayList<Post>();
        firestore = FirebaseFirestore.getInstance();

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        adapter =new PostRecyclerAdapter(postList);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                if (reachedBottom){
                    loadMorePost();
                }
            }
        });

        Query query = firestore.collection("Posts").orderBy("timestamp",Query.Direction.DESCENDING).limit(3);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()){
                    if (isPageFirstTimeLoaded){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() -1);
                    }
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()){
                        if (doc.getType() == DocumentChange.Type.ADDED){
                            Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                            if (isPageFirstTimeLoaded){
                                postList.add(post);
                            }else {
                                postList.add(0,post);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                    isPageFirstTimeLoaded = false;
                }
            }
        });
        return view;
    }
    public void loadMorePost(){
        Query nextQuery = firestore.collection("Posts")
                .orderBy("timestamp",Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()){
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() -1);
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()){
                        if (doc.getType() == DocumentChange.Type.ADDED){
                            Post post = doc.getDocument().toObject(Post.class).withId(doc.getDocument().getId());
                            postList.add(post);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }
}
