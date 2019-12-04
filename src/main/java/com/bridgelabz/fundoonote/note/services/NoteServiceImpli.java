package com.bridgelabz.fundoonote.note.services;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.bridgelabz.fundoonote.config.Model;
import com.bridgelabz.fundoonote.customexception.Exceptions;
import com.bridgelabz.fundoonote.label.model.Label;
import com.bridgelabz.fundoonote.label.repository.LabelRepository;
import com.bridgelabz.fundoonote.model.User;
import com.bridgelabz.fundoonote.note.dto.NoteDto;
import com.bridgelabz.fundoonote.note.model.Note;
import com.bridgelabz.fundoonote.note.repository.NoteRepository;
import com.bridgelabz.fundoonote.repository.UserRepository;
import com.bridgelabz.fundoonote.response.Response;
import com.bridgelabz.fundoonote.utility.Jwt;
import com.bridgelabz.fundoonote.utility.Utility;
/**@Purpose Fundoo Api
 * @author Ankush Kumar Agrawal
 *@Date 20 Nov 2019
 */

//@PropertySource("classpath:messages.properties")
@Service
public class NoteServiceImpli implements NoteServices{
	
	/**
	 * @see com.bridgelabz.fundoonote.note.utility
	 * it has used to convert token to mail and mail to token
	 */
	@Autowired
	Jwt noteJwt;
	
	/**
	 * @see  com.bridgelabz.fundoonote.Repository
	 */
	@Autowired
	private UserRepository userRepo;
	/**
	 * @see  com.bridgelabz.fundoonote.note.Repository
	 */
	@Autowired
	private	NoteRepository noteRepo; 

	@Autowired
	Environment environment;
	/**@purpose to create a new note
	 *@param notedto
	 *@param toekn
	 *@return a simple Message
	 */
	
	@Autowired
	LabelRepository labelRepo;
	@Override
	public ResponseEntity<Object> addNote(NoteDto notedto, String token) {
		String email= noteJwt.getUserToken(token);
		User user = userRepo.findByEmail(email);
		if(user==null) {
			throw new Exceptions("UserNotFoundException");
		}
			Note note = Model.getModel().map(notedto,Note.class);
			note.setCreatedDate(LocalDate.now());
			note.setEmail(email);
			noteRepo.save(note);	
			
			return new ResponseEntity<>(environment.getProperty("ADD_NOTE"),HttpStatus.OK);
		}

	/**@purpose delete the note from the user
	 *@param notedto
	 *@param toekn
	 *@return a simple Message
	 */
	@Override
	public ResponseEntity<Object> deleteNote(String token, String id) {
		
		Note note = isNote(token,id);
		noteRepo.delete(note);
		 return new ResponseEntity<>(environment.getProperty("Delete_NOTE"),HttpStatus.MOVED_PERMANENTLY);
		
	}
	/**
	 * @purpose to update/edit the note 
	 *@param notedto
	 *@param token
	 *@return a simple Message
	 */
	@Override
	public ResponseEntity<Object> updateNote(String id,NoteDto notedto, String token) {
	
		Note note = isNote(token,id);
		note.setUpdatedDate(LocalDate.now());
		note.setDescription(notedto.getDescription());
		note.setTitle(notedto.getTitle());
		noteRepo.save(note);
		return new ResponseEntity<>(environment.getProperty("update"),HttpStatus.OK);
	}
	/**
	 *To get all the the notes
	 *
	 *@return total available notes
	 */
	
	@Override
	public List<Note> getAllNote(String email) {

		return noteRepo.findByEmail(email);
	}
	
	@Override
	public List<Note> showAll(){
		return noteRepo.findAll();
	}
	
	/**
	 *METHOD FOR SORT THE NOTE BY TITLE
	 */
	@Override
	public List<Note> sortByTitle(){
		List<Note> sorted = noteRepo.findAll().stream()
				.sorted(Comparator.comparing(Note::getTitle)).parallel().collect(Collectors.toList());
	
		return sorted;
	}
	/**
	 *METHOD FOR SORT  THE NOTE BY CREATED DATE
	 */
	@Override
	public List<Note> sortByCreatedDate(){
		
		List<Note> sorted = noteRepo.findAll()
			.stream().sorted(Comparator.comparing(Note::getCreatedDate)).parallel()
				.collect(Collectors.toList());
		return sorted;
	}
	/**
	 *
	 */
	@Override
	public ResponseEntity<Object> isPinned(String id,String token) {
		Note note = isNote(token,id);
		note.setPinned(!note.isPinned());
		return new ResponseEntity<>(environment.getProperty("Sucess"),HttpStatus.OK);
	}
		/**
		 *
		 */
		@Override
	public ResponseEntity<String> isTrashed(String id,String token) {
		Note note = isNote(token,id);
		note.setTrashed(!note.isTrashed());
		return new ResponseEntity<>(environment.getProperty("Sucess"),HttpStatus.OK);
	}
	/**
	 *
	 */
	@Override
	public ResponseEntity<Object> isArchieved(String id,String token) {
		Note note = isNote(token,id);
		note.setTrashed(!note.isArchieved());
		return new ResponseEntity<>(environment.getProperty("Sucess"),HttpStatus.OK);
	}
		
	/**
	 * @param token
	 * @param noteId
	 * @param collobrate email
	 * @return Response message
	 * 
	 */

	public ResponseEntity<Object> addCollobrate(String token,String noteId,String collabemail){
		
		User user = userRepo.findByEmail(collabemail);
		Note note = isNote(token,noteId);
		boolean status = note.getListOfcollobarator().contains(collabemail);
		if(user ==null) {
			throw new Exceptions("UserNotFoundExceptions");
		}
		if(status) {
			return new ResponseEntity<>(environment.getProperty("already"),HttpStatus.OK);
		}
		note.getListOfcollobarator().add(collabemail);
		System.out.println("hello1");
		noteRepo.save(note);
		return new ResponseEntity<>(environment.getProperty("Sucess"),HttpStatus.OK);
	}
	
	/**
	 *REMOVE COLLOBRATOR
	 */
	public ResponseEntity<Object> removeCollobrate(String token ,String noteId,String rcollabemail){
		Note note = isNote(token,noteId);
		note.getListOfcollobarator().remove(rcollabemail);
		return new ResponseEntity<>(environment.getProperty("CHECK"),HttpStatus.OK);
		
	}
	
	/**
	 *METHOD FOR ADD LABEL 
	 */
	public ResponseEntity<Object> addLabel(String email ,String noteid,String lblid) {
		List<Note> listOfNote = noteRepo.findByEmail(email);
		List<Label> listOfLabel = labelRepo.findByEmail(email);	
		Note note = listOfNote.stream().filter(i->i.getId().equals(noteid)).findAny().orElse(null);
		Label label = listOfLabel.stream().filter(i->i.getLabelid().equals(lblid)).findAny().orElse(null);
		if(note==null && label==null) {
			throw new Exceptions("NoteOrLabelNotFoundExceptions");
		}
		note.getListOfLabel().add(label);
		label.getListOfNote().add(note);
		noteRepo.save(note);
		labelRepo.save(label);
		return new ResponseEntity<>(environment.getProperty("Sucess"),HttpStatus.OK);
	}

	@Override
	public Response addReminder(String token, String noteId, String date) throws ParseException{
		Note note = isNote(token,noteId);

		note.setReminder(Utility.dateFormat(date));
		noteRepo.save(note);
		
		return new Response(200,environment.getProperty("ReminderAdded"),HttpStatus.OK);
	}



	@Override
	public Response removeReminder(String token, String noteId) {

		String email = noteJwt.getUserToken(token);
		List<Note> notes= noteRepo.findByEmail(email);
		Note note = notes.stream().filter(i->i.getId().equals(noteId)).findAny().orElse(null);
		if(note==null) {
			throw new Exceptions("NoteNotFoundException");
		}
		note.setReminder(null);
		noteRepo.save(note);
		return new Response(200,environment.getProperty("ReminderRemoved"),HttpStatus.OK);
	}



	@Override
	public Response updateReminder(String token, String noteId, String date) {
		String email = noteJwt.getUserToken(token);
		List<Note> notes= noteRepo.findByEmail(email);
		Note note = notes.stream().filter(i->i.getId().equals(noteId)).findAny().orElse(null);
		if(note==null) {
			throw new Exceptions("NoteNotFoundException");
		}
		try {
			note.setReminder(Utility.dateFormat(date));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		noteRepo.save(note);
		
		return new Response(200,environment.getProperty("ReminderUpdate"),HttpStatus.OK);
	}
	/**
	 *
	 */
	@Override
	public Note isNote(String token,String id) {
		String email = noteJwt.getUserToken(token);
		List<Note> notes = noteRepo.findByEmail(email);
		Note note = notes.stream().filter(i->i.getId().equals(id)).findAny().orElse(null);
		if(note==null) {
			throw new Exceptions("NoteNotFoundException");
		}
		return note;
	}



}
	


