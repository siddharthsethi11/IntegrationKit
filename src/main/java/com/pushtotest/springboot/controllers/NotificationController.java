package com.pushtotest.springboot.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.pushtotest.springboot.interfaces.UserService;
import com.pushtotest.springboot.model.BettingGames;
import com.pushtotest.springboot.model.BettingOdds;
import com.pushtotest.springboot.model.Notifications;
import com.pushtotest.springboot.model.UserIdentification;
import com.pushtotest.springboot.repository.interfaces.BettingGamesRepository;
import com.pushtotest.springboot.repository.interfaces.BettingOddsRepository;
import com.pushtotest.springboot.repository.interfaces.NotificationRepository;

@Controller
public class NotificationController {
	
	@Autowired
    private UserService userService;
	
	@Autowired
    private BettingGamesRepository gamesRepository;
	
	@Autowired
    private BettingOddsRepository oddsRepository;
	
	@Autowired
    private NotificationRepository nRepository;

    @MessageMapping("/notify")
    @SendTo("/topic/notifications")
    public String greeting(UserIdentification uId) throws Exception {
        Thread.sleep(1000); // simulated delay
        //userService.updateBalance(userId, balance);
        List<Notifications> nList = nRepository.findByUserIdAndStatus(uId.getId(), "UNREAD");
        Iterator<Notifications> itr = nList.iterator();
        
        JSONArray jAr = new JSONArray();
        while(itr.hasNext()){
        	Notifications notif = itr.next();
        	JSONObject jObj = new JSONObject();
        	jObj.put("nId", notif.getId());
        	Date cDate = notif.getCreatedDate();
        	SimpleDateFormat sdf = new SimpleDateFormat("MM-DD-YYYY");
        	jObj.put("date", sdf.format(cDate));
        	jObj.put("title", notif.getTitle());
        	jObj.put("description", notif.getDescription());
        	jObj.put("status", notif.getStatus());
        	
        	jAr.put(jObj);
        }
        return jAr.toString();
    }
    
    @MessageMapping("/getOdds")
    @SendTo("/topic/bettingodds")
    public String getOdds(UserIdentification uId) throws JSONException {
    	//List<BettingGames> bettingGamesList = gamesRepository.findAllDistinctByType();
    	List<BettingGames> bettingGamesList = gamesRepository.findAll();
    	Iterator<BettingGames> itr = bettingGamesList.iterator();
    	
    	JSONArray jAr = new JSONArray();
    	
    	while(itr.hasNext()){
    		BettingGames bg = itr.next();
	    	List<BettingGames> gamesList = gamesRepository.findByType(bg.getType());
	    	Iterator<BettingGames> itr1 = gamesList.iterator();
	    	
	    	while(itr.hasNext()){
	    		BettingGames bg1 = itr1.next();
		    	List<BettingOdds> bettingOddsList = oddsRepository.findByGameIdAndStatus(bg1.getGameId(), "active");
		    	Iterator<BettingOdds> itr2 = bettingOddsList.iterator();
		    	while(itr2.hasNext()){
		    		JSONObject jObj = new JSONObject();
		    		BettingOdds bo = itr2.next();
		    		jObj.put("game_id", bo.getGameId());
		    		JSONArray totalList = new JSONArray((bo.getLock() != null) ? bo.getLock() : "[]");
		    		JSONArray homeList = new JSONArray((bo.getHome() != null) ? bo.getHome() : "[]");
		    		JSONArray drawList = new JSONArray((bo.getDraw() != null) ? bo.getDraw() : "[]");
		    		JSONArray awayList = new JSONArray((bo.getAway() != null) ? bo.getAway() : "[]");
		    		
		    		jObj.put("homebet", homeList.length() + "/" + totalList.length());
		    		jObj.put("drawbet", drawList.length() + "/" + totalList.length());
		    		jObj.put("awaybet", awayList.length() + "/" + totalList.length());
		    		jAr.put(jObj);
		    	}
	    	}
    	}
    	//return jAr.toString();
    	return uId.getId() + "=" + new Date().getTime();
    }

}