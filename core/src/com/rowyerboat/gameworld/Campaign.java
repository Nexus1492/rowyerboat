package com.rowyerboat.gameworld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.rowyerboat.gameworld.Mission.MissionID;
import com.rowyerboat.helper.Settings;

public class Campaign {
	private static HashMap<CampaignID, Campaign> campaigns;

	public CampaignID id;
	public String name;
	public ArrayList<Mission> campaignMissions;
	public int accomplishedMissions;
	public boolean isFinished;

	public enum CampaignID {
		TutorialCampaign, Campaign01, Campaign02;

		public String getName() {
			switch (this) {
			case Campaign01:
				return "Campaign01";
			case Campaign02:
				return "Placerholder Campaign";
			case TutorialCampaign:
				return "Tutorial";
			}
			return "Campaign not found.";
		}
	}

	public static void init() {
		campaigns = new HashMap<CampaignID, Campaign>();
		for (CampaignID id : CampaignID.values())
			campaigns.put(id, new Campaign(id));
	}

	public static Campaign getCampaign(CampaignID id) {
		return campaigns.get(id);
	}

	public static Campaign getCampaign(MissionID id) {
		return campaigns.get(Mission.getMission(id).campaignID);
	}

	private Campaign(CampaignID id) {
		this.id = id;
		name = id.toString();
		accomplishedMissions = Settings.campaignProgress.getInteger(id.toString(), 0);
		campaignMissions = new ArrayList<Mission>();
		switch (id) {
		case Campaign01:
			addMission(MissionID.Pottery);
			addMission(MissionID.JaguarTeeth);
			addMission(MissionID.JaguarTeeth2);
			break;
		case Campaign02:
			//campaignMissions.add(Mission.getMission(MissionID.Placeholder));
			break;
		case TutorialCampaign:
			addMission(MissionID.Tutorial0);
			addMission(MissionID.Tutorial1);
			break;
		}
		for (Mission mis : campaignMissions) // set the corresponding campaignIDs
			mis.campaignID = id;
		updateProgress();
	}
	
	private void addMission(MissionID ID) {
		campaignMissions.add(Mission.getMission(ID));
	}

	public Mission nextMission(MissionID currMis) {
		int nextMis = campaignMissions.indexOf(Mission.getMission(currMis)) + 1;
		if (nextMis >= campaignMissions.size() || nextMis < 0)
			return null;
		return campaignMissions.get(campaignMissions.indexOf(Mission.getMission(currMis)) + 1);
	}

	/**
	 * Go through the preferences,
	 * count every mission (as key) which have this specific campaign as ID (as value),
	 * set the isFinished value.
	 */
	public void updateProgress() {
		accomplishedMissions = 0;
		for (Object entry : Settings.campaignProgress.get().values()) {
			if (entry.toString().equals(this.id.toString()))
				accomplishedMissions++;
		}
		isFinished = accomplishedMissions == campaignMissions.size() && campaignMissions.size() != 0;
		Settings.campaignProgress.putInteger(this.id.toString(), accomplishedMissions);
		Settings.campaignProgress.flush();
	}
}
