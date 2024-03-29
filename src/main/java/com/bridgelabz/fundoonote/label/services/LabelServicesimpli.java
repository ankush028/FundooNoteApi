package com.bridgelabz.fundoonote.label.services;
/**@Purpose Fundoo Api
 * @author Ankush Kumar Agrawal
 *@Date 20 Nov 2019
 */
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.bridgelabz.fundoonote.config.Model;
import com.bridgelabz.fundoonote.customexception.Exceptions;
import com.bridgelabz.fundoonote.label.dto.LabelDto;
import com.bridgelabz.fundoonote.label.model.Label;
import com.bridgelabz.fundoonote.label.repository.LabelRepository;
import com.bridgelabz.fundoonote.model.User;
import com.bridgelabz.fundoonote.note.model.Note;
import com.bridgelabz.fundoonote.note.repository.NoteRepository;
import com.bridgelabz.fundoonote.repository.UserRepository;
import com.bridgelabz.fundoonote.response.Response;
import com.bridgelabz.fundoonote.utility.Jwt;


@Service
public class LabelServicesimpli implements LabelServices{

	/**
	 * @see com.bridgelabz.fundoonote.utility
	 */
	@Autowired
	private Jwt jwtToken;
	/**
	 * @see com.bridgelabz.fundoonote.label.repository
	 */
	@Autowired
	private LabelRepository labelRepo;
	/**
	 * @see com.bridgelabz.fundoonote.repository
	 */
	@Autowired
	private UserRepository userRepo;
	/**
	 * @see com.bridgelabz.fundoonote.note.repository
	 */
	@Autowired
	private NoteRepository noteRepo;
	
	@Autowired
	Environment environment;
	
	/**
	 * @param labeldto
	 * @param token
	 *@purpose METHOD ADD NEW LABEL
	 *@return Response 
	 */
	private String exceptionkey ="labelException";
	@Override
	public Response addLabel(LabelDto labeldto, String token) {

		String email = jwtToken.getUserToken(token);
		User user = userRepo.findByEmail(email);
 		if(user==null) {
			throw new Exceptions(environment.getProperty("userException"));
		}
		Label label =Model.getModel().map(labeldto,Label.class);
		label.setCreatedDate(LocalDate.now());
		label.setEmail(email);
		labelRepo.save(label);
		return new Response(200,environment.getProperty("Addedlabel"),HttpStatus.OK);
	}

	/**
	 * @purpose to Delete a label
	 * @param token
	 * @param id
	 * @return A Response message operation has performed or not
	 */
	@Override
	public Response deleteLabel(String token, String id) {

		String email = jwtToken.getUserToken(token);

		List<Label> listOflabel =  labelRepo.findByEmail(email);

		Optional<Label> label = listOflabel.stream().filter(i->i.getLabelid().equals(id)).findAny();
		if(!label.isPresent()) {	
		throw new Exceptions(environment.getProperty(exceptionkey));
	}
		labelRepo.delete(label.get());
		return new Response(200,environment.getProperty("delete_label"),HttpStatus.OK);
	}

	/**
	 * @param token
	 * @param id
	 * @param labeldto
	 * @return A Response message operation has performed or not
	 */
	@Override
	public Response updateLabel(String token,String id,LabelDto labeldto) {
		String email = jwtToken.getUserToken(token);
		List<Label> listOflabel =  labelRepo.findByEmail(email);
		Optional<Label> label = listOflabel.stream().filter(i->i.getLabelid().equals(id)).findAny();
		if(!label.isPresent()) { 
		throw new Exceptions(environment.getProperty(exceptionkey));
		}

		label.get().setUpdatedDate(LocalDate.now());		
		label.get().setLabeltitle(labeldto.getLabeltitle());	
		labelRepo.save(label.get());
		
		return new Response(200,environment.getProperty("Update_label"),HttpStatus.OK);
	}

	/**
	 * @return A Response message operation has performed or not
	 */
	@Override
	public List<Label> getAlllabel() {

		return labelRepo.findAll();
	}

	/**
	 * @return all label present in database
	 */
	@Override
	public List<Label> sortedByTitle() {

		return labelRepo.findAll().stream()
				.sorted(Comparator.comparing(Label::getLabeltitle)).collect(Collectors.toList());
	}

	/**
	 * @return all sorted list in sorted manner
	 */
	@Override
	public List<Label> sortedByDate() {

		return labelRepo.findAll().stream()
				.sorted(Comparator.comparing(Label::getCreatedDate)).collect(Collectors.toList());
	}
	/**
	 * @param lblid
	 * @param email
	 * @param noteid
	 * @purpose METHOD ADD NEW Note in label
	 * @return Response 
	 */
	@Override
	public Response addNote(String email ,String noteid,String lblid) {

		List<Note> listOfNote = noteRepo.findByEmail(email);
		List<Label> listOfLabel = labelRepo.findByEmail(email);
		Optional<Note> note = listOfNote.stream().filter(i->i.getId().equals(noteid)).findAny();
		Optional<Label> label = listOfLabel.stream().filter(i->i.getLabelid().equals(lblid)).findAny();
		if(!note.isPresent()) { 
			throw new Exceptions(environment.getProperty("noteException"));
		}
		if(!label.isPresent()) {
			throw new Exceptions(environment.getProperty(exceptionkey));
		}
		label.get().getListOfNote().add(note.get());
		labelRepo.save(label.get());
		
		return new Response(200,environment.getProperty("Sucess"),HttpStatus.OK);
	}


}
