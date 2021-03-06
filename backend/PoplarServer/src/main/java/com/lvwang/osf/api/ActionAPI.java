package com.lvwang.osf.api;

import java.util.HashMap;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lvwang.osf.model.Notification;
import com.lvwang.osf.model.User;
import com.lvwang.osf.service.FollowService;
import com.lvwang.osf.service.InterestService;
import com.lvwang.osf.service.LikeService;
import com.lvwang.osf.service.NotificationService;
import com.lvwang.osf.service.UserService;
import com.lvwang.osf.util.Dic;
import com.lvwang.osf.util.Property;
import com.lvwang.osf.web.RequestAttribute;


@Controller
@RequestMapping("/api/v1/action/") 
public class ActionAPI {

	@Autowired
	@Qualifier("userService")
	private UserService userService;
	
	@Autowired
	@Qualifier("likeService")
	private LikeService likeService;
	
	@Autowired
	@Qualifier("interestService")
	private InterestService interestService;
	
	@Autowired
	@Qualifier("followService")
	private FollowService followService;
	
	@Autowired
	@Qualifier("notificationService")
	private NotificationService notificationService;
	
	@ResponseBody
	@RequestMapping("/{author}/do/like/{object_type}/{object_id}")
	public Map<String, String> like(@PathVariable("author") int author,
									@PathVariable("object_type") int object_type,
									@PathVariable("object_id") int object_id,
					  				@RequestAttribute("uid") Integer id){
		
		User me = (User)userService.findById(id);
				
		likeService.like(me.getId(), object_type, object_id);
		Map<String, String> ret = new HashMap<String, String>();
		ret.put("status", Property.SUCCESS_LIKE);
		
		Notification notification = new Notification(Dic.NOTIFY_TYPE_LIKE, 
													 0, 
													 object_type, 
													 object_id, 
													 author, 
													 me.getId()
													 );
		notificationService.doNotify(notification);
		return ret;
	}
	
	
	@ResponseBody
	@RequestMapping("/undo/like/{object_type}/{object_id}")
	public Map<String, String> undolike(@PathVariable("object_type") int object_type,
										@PathVariable("object_id") int object_id,
										@RequestAttribute("uid") Integer id){
		User me = (User)userService.findById(id);
		likeService.undoLike(me.getId(), object_type, object_id);
		Map<String, String> ret = new HashMap<String, String>();
		ret.put("status", Property.SUCCESS_LIKE_UNDO);
		return ret;
	}
	
	@ResponseBody
	@RequestMapping("/is/follow/{user_id}")
	public Map<String, Object> isFollow(@PathVariable("user_id") int user_id, @RequestAttribute("uid") Integer id) {
		Map<String, Object> map = new HashMap<String, Object>();
		boolean result = followService.isFollowing(id, user_id);
		map.put("status", Property.SUCCESS);
		map.put("isfollow", result);
		return map;
	}
	
	@ResponseBody
	@RequestMapping("/do/follow/{user_id}")
	public Map<String, Object> follow(@PathVariable("user_id") int user_id, @RequestAttribute("uid") Integer id) {
		User user = (User) userService.findById(id);
		Map<String, Object> map = followService.newFollowing(user.getId(), 
															 user.getUser_name(), 
															 user_id, 
															 userService.findById(user_id).getUser_name());
		Notification notification = new Notification(Dic.NOTIFY_TYPE_FOLLOW, 
												     0, 
												     Dic.OBJECT_TYPE_USER, 
												     user_id, 
												     user_id, 
												     user.getId());
		notificationService.doNotify(notification);
		return map;
	}
	
	@ResponseBody
	@RequestMapping("/undo/follow/{user_id}")
	public Map<String, Object> undoFollow(@PathVariable("user_id") int user_id, @RequestAttribute("uid") Integer id) {
		Map<String, Object> map = followService.undoFollow(id, user_id);
		return map;
	}
	
	/**
	 * 对某个标签感兴趣
	 */
	@ResponseBody
	@RequestMapping("/do/followtag/{tag_id}")
	public Map<String, Object> interest(@PathVariable("tag_id") int tag_id, @RequestAttribute("uid") Integer id) {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		User user = (User) userService.findById(id);
		interestService.interestInTag(user.getId(), tag_id);
				
		ret.put("status", Property.SUCCESS_INTEREST);
		return ret;
	}
	
	
	/**
	 * 
	 */
	@ResponseBody
	@RequestMapping("/undo/followtag/{tag_id}")
	public Map<String, Object> undoInterest(@PathVariable("tag_id") int tag_id, @RequestAttribute("uid") Integer id) {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		User user = (User) userService.findById(id);
		interestService.undoInterestInTag(user.getId(), tag_id);
		
		ret.put("status", Property.SUCCESS_INTEREST_UNDO);
		return ret;
	}
	
	@ResponseBody
	@RequestMapping("/tag/{tag_id}/details")
	public Map<String, Object> detatils(@PathVariable("tag_id") int tag_id, @RequestAttribute("uid") Integer id) {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		ret.put("isfollow", interestService.hasInterestInTag(id, tag_id));
		ret.put("status", Property.SUCCESS);
		
		return ret;
	}
	
}
