package andy.firebasedemo.manager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import andy.firebasedemo.object.Member;

/**
 * Created by andyli on 2016/11/5.
 */

public class MemberManager implements ChildEventListener {
	static  MemberManager instance;
	private HashMap<String, Member> memberCache = new HashMap<>();


	public static MemberManager getInstance(){
		if(instance == null){
			instance = new MemberManager();
		}
		return instance;
	}
	public void registerUserListener() {
		FirebaseDatabase.getInstance().getReference("users").addChildEventListener(this);
	}

	public void unRegisterUserListener() {
		FirebaseDatabase.getInstance().getReference("users").removeEventListener(this);
	}

    public void remove(String uid){
		synchronized (memberCache) {
			memberCache.remove(uid);
		}
	}

	public void updateMember(String uid, Member member){
		synchronized (memberCache) {
			memberCache.put(uid, member);
		}
	}

	public List<Member> getOffLineMember(){
		List<Member> members = new ArrayList<>();
		for(HashMap.Entry<String, Member> entry :memberCache.entrySet()){
			if(entry != null){
				Member member = entry.getValue();
				if(member.status == Member.STATUS_OFFLINE){
					members.add(member);
				}
			}
		}
		return members;
	}

	public Member getMemberById(String uid){
		return memberCache.get(uid);
	}

	public void clear(){
		synchronized (memberCache) {
			memberCache.clear();
		}
	}


	@Override
	public void onChildAdded(DataSnapshot dataSnapshot, String s) {
		Member member = dataSnapshot.getValue(Member.class);
		updateMember(dataSnapshot.getKey(), member);
	}

	@Override
	public void onChildChanged(DataSnapshot dataSnapshot, String s) {
		Member member = dataSnapshot.getValue(Member.class);
		updateMember(dataSnapshot.getKey(), member);
	}

	@Override
	public void onChildRemoved(DataSnapshot dataSnapshot) {
		remove(dataSnapshot.getKey());
	}

	@Override
	public void onChildMoved(DataSnapshot dataSnapshot, String s) {

	}


	@Override
	public void onCancelled(DatabaseError databaseError) {

	}
}
