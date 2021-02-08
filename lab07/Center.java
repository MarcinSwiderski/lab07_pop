package lab07;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.IntStream;


import communication.ICenter;
import communication.IDesigner;
import communication.IMonitor;
import communication.IStand;
import support.Answer;
import support.CustomException;
import support.Question;

public class Center implements ICenter {

	private int iDesignerId = 0;
	private IDesigner iDesigner = null;

	private int nextIdentifier = 0;
	private int nextUserIdentifier = 0;
	private final Map<Integer, String> visitorNames = new HashMap<>();
	private final Map<Integer, IStand> stands = new HashMap<>();
	private final Map<Integer, IMonitor> monitors = new HashMap<>();

	private final List<Question> questions = new ArrayList<>();
	private final List<Answer> answers = new ArrayList<>();

	public Center() {
		try {
			Registry reg = LocateRegistry.createRegistry(4000);
			reg.rebind("Center", UnicastRemoteObject.exportObject(this, 0));
			System.out.println("Center is ready");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
	    new Center();
	}

	@Override
	public int signIn(String visitorName) throws RemoteException {
		visitorNames.put(nextUserIdentifier, visitorName);
		return nextUserIdentifier++;
	}

	@Override
	public void signOut(int visitorId) throws RemoteException, CustomException {
        if(!visitorNames.containsKey(visitorId)) {
        	throw new CustomException("Signing out a nonexistant visitor");
		}

        visitorNames.remove(visitorId);
	}

	@Override
	public Question[] getQuestions() throws RemoteException {
	    return questions.toArray(Question[]::new);
	}

	@Override
	public boolean[] checkAnswers(int userId, Answer[] a) throws RemoteException, CustomException {

	    boolean[] corrects = new boolean[a.length];
	    int amountCorrect = 0;
		for(int i = 0; i < corrects.length; i++) {
			corrects[i] = answers.get(i).answer.equals(a[i].answer);
			if(corrects[i])
				amountCorrect++;
		}
		int percentageScore = Math.round((100f * amountCorrect) / (1f * a.length));

		for(IMonitor monitor : monitors.values()) {
		    monitor.setScore(visitorNames.get(userId), percentageScore);
		}
		return corrects;
	}

	@Override
	public void addQA(Question[] q, Answer[] a) throws RemoteException, CustomException {
        questions.addAll(Arrays.asList(q));
        answers.addAll(Arrays.asList(a));
	}

	@Override
	public IStand[] getStands() throws RemoteException {
        return stands.values().toArray(IStand[]::new);
	}

	@Override
	public int connect(IDesigner id) throws RemoteException {
		iDesigner = id;
		return nextIdentifier++;
	}

	@Override
	public int connect(IStand is) throws RemoteException {
		stands.put(nextIdentifier, is);

		if(iDesigner != null)
			iDesigner.notify(nextIdentifier, true);

		return nextIdentifier++;
	}

	@Override
	public int connect(IMonitor im) throws RemoteException {
	    monitors.put(nextIdentifier, im);
		return nextIdentifier++;
	}

	@Override
	public void disconnect(int identifier) throws RemoteException, CustomException {
		// TODO Auto-generated method stub
		if(stands.containsKey(identifier)) {
			stands.remove(identifier);

			if(iDesigner != null)
				iDesigner.notify(identifier, false);
		} else if(monitors.containsKey(identifier)) {
			monitors.remove(identifier);
		} else if(identifier == iDesignerId) {
			iDesigner = null;
		} else {
			throw new CustomException("Trying to disconnect an entity that doesn't exist.");
		}
	}
}
