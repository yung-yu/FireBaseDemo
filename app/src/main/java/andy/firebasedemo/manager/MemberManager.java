package andy.firebasedemo.manager;

import java.util.HashMap;

import andy.firebasedemo.object.Member;

/**
 * Created by andyli on 2016/11/5.
 */

public class MemberManager {
	static  MemberManager instance;
	private HashMap<String, Member> memberCache = new HashMap<>();

	public static MemberManager getInstance(){
		if(instance == null){
			instance = new MemberManager();
		}
		return instance;
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

	public Member getMemberById(String uid){
		return  memberCache.get(uid);
	}

	public void clear(){
		synchronized (memberCache) {
			memberCache.clear();
		}
	}
}
