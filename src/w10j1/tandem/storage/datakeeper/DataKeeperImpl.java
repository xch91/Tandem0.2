/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package w10j1.tandem.storage.datakeeper;

import com.mdimension.jchronic.utils.Span;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import static w10j1.tandem.storage.datakeeper.TaskComparator.*;
import w10j1.tandem.storage.datakeeper.api.DataKeeper;
import w10j1.tandem.storage.task.TaskImpl;
import w10j1.tandem.storage.task.api.Task;

/**
 * 
 * @author Chris
 */
public class DataKeeperImpl implements DataKeeper {

	private ArrayList<Task> taskList = new ArrayList<Task>();
	private ArrayList<Task> searchList = new ArrayList<Task>();
	private Task tempTask;

	private enum undoState {
		NULL, ADD, DEL
	};

	private undoState rollBack = undoState.NULL;
	private SimpleDateFormat formatter = new SimpleDateFormat(
			"dd-MM-yyyy HH:mm");

	public DataKeeperImpl() {
		taskList = new ArrayList<Task>();
	}

	@Override
	public String ascDue() {
		Collections.sort(searchList, ascending(DUE_SORT));
		return resultString();
	}

	@Override
	public String decDue() {
		Collections.sort(searchList, decending(DUE_SORT));
		return resultString();
	}

	@Override
	public String ascPriority() {
		Collections.sort(searchList, ascending(PRIORITY_SORT));
		return resultString();
	}

	@Override
	public String decPriority() {
		Collections.sort(searchList, decending(PRIORITY_SORT));
		return resultString();
	}

	@Override
	public String memToFile() {
		Collections.sort(getTaskList(), ascending(DUE_SORT));
		StringBuilder sb = new StringBuilder();
		for (Task t : getTaskList()) {
			sb.append(t.toString());
		}
		return sb.toString();
	}

	@Override
	public void fileToMem(String fromFile) {
		String[] tempList = fromFile.split("\r\n");
		for (String task : tempList) {
			// Getting due
			String[] taskDetail = task.split("\\|");
			Calendar time = Calendar.getInstance();
			try {
				time.setTime(formatter.parse(taskDetail[0]));
			} catch (ParseException ex) {
				Logger.getLogger(DataKeeperImpl.class.getName()).log(
						Level.SEVERE, null, ex);
			}
			getTaskList().add(new TaskImpl(time, taskDetail[1]));
		}
	}

	@Override
	public String resultString() {
		assert (searchList != null);
		assert (searchList.size() >= 0);
		StringBuilder sb = new StringBuilder();
		for (Task t : searchList) {
			sb.append(searchList.indexOf(t)).append(". ").append(t.toString())
					.append("\r\n");
		}
		return sb.toString();
	}

	@Override
	public void addTask(Task task) {
		tempTask = task;
		getTaskList().add(task);
		rollBack = undoState.ADD;
	}

	@Override
	public void searchTask(String keywords) {
		String[] kw = keywords.split("\\s");
		searchList.clear();
		for (Task task : getTaskList()) {
			boolean hasAllWords = true;
			for (String word : kw) {
				if (!task.getDesc().contains(word)) {
					hasAllWords = false;
					break;
				}
			}
			if (hasAllWords) {
				searchList.add(task);
			}
		}
	}

	@Override
	public void searchTask(Span interval) {
		searchList.clear();
		for (Task task : getTaskList()) {
			if (task.getDue().compareTo(interval.getBeginCalendar()) >= 0
					&& task.getDue().compareTo(interval.getEndCalendar()) <= 0) {
				searchList.add(task);
			}
		}
	}

	@Override
	public void removeTask(Task task) {
		tempTask = task;
		getTaskList().remove(task);
		rollBack = undoState.DEL;
	}

	@Override
	public boolean undo() {
		switch (rollBack) {
		case NULL:
			return false;
		case ADD:
			removeTask(tempTask);
			rollBack = undoState.NULL;
		case DEL:
			addTask(tempTask);
			rollBack = undoState.NULL;
		}
		assert (rollBack == undoState.NULL);
		return true;
	}

	@Override
	public ArrayList<Task> getTaskList() {
		return taskList;
	}

	@Override
	public ArrayList<Task> getSearchList() {
		return searchList;
	}

	public undoState getRollBack() {
		return rollBack;
	}
	
	public Task getTempTask() {
		return tempTask;
	}
}