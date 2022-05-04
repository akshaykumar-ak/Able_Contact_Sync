package com.example.ablecontactsync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Button sync_btn,del_btn;
    String firstName;
    String lastName;
    String number;
    ProgressBar progressBar;
    int count;
    int cres;
    TextView progressText;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String[] PERMISSIONS;

    public boolean isTodayData(String dt){
        String ml = dt.substring(18,28);
        long mls = Long.parseLong(ml)*1000;
        String d = new Date(mls).toString();
        String Month = d.substring(4,7);
        String Date = d.substring(8,10);
        String Year = d.substring(29,34);
        String curD = new Date().toString();
        String curMonth = curD.substring(4,7);
        String curDate = curD.substring(8,10);
        String curYear = curD.substring(29,34);
        return curDate.equals(Date) && curMonth.equals(Month) && curYear.equals(Year);
    }

    public void getData(){
        db.collection("Contacts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            count=1;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                cres = task.getResult().size();
                                progressBar.setMax(cres);
                                progressBar.setProgress(count);
                                if(count==cres){
                                    progressText.setText("All Contacts Synced");
                                }
                                firstName = Objects.requireNonNull(document.get("FirstName")).toString();
                                lastName = Objects.requireNonNull(document.get("LastName")).toString();
                                number = Objects.requireNonNull(document.get("Number")).toString();
                                String tm = Objects.requireNonNull(document.get("addedOn")).toString();

                                if(isTodayData(tm)){
                                        try {
                                            testBatchInsertion();
                                        } catch (RemoteException | OperationApplicationException e) {
                                            e.printStackTrace();
                                        }
                                }
                                count++;
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Error Getting Data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void testBatchInsertion() throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            generateSampleProviderOperation(ops, firstName, lastName,number);
                this.getContentResolver().applyBatch(ContactsContract.AUTHORITY,ops);
//                ops.clear();
    }
    private void generateSampleProviderOperation(ArrayList<ContentProviderOperation> ops,String firstName,String lastName,String number){
        int backReference = ops.size();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
                .build()
        );
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReference)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
                .build()
        );
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReference)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MAIN)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                    .build()
            );
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sync_btn = findViewById(R.id.sync_btn);
        del_btn = findViewById(R.id.del_btn);
        progressText = findViewById(R.id.progressText);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setProgress(0);
        PERMISSIONS = new String[] {
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
        };
        sync_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });
        
        
        del_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Not Yet Implemented", Toast.LENGTH_SHORT).show();
            }
        });
    }
}