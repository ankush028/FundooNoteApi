package com.bridgelabz.fundoonote.label.model;
/**@Purpose Fundoo Api
 * @author Ankush Kumar Agrawal
 *@Date 20 Nov 2019
 */
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import com.bridgelabz.fundoonote.note.model.Note;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
//Model Class of label 
//in this class all field of label
@Data
public class Label {
	
	//uniquely id Generation
	@Id
	private String labelid;
	
	private String email;
	
	private String labeltitle;
	
	private LocalDate createdDate;
	
	private LocalDate updatedDate;
	
	//@purpose DBref is to refer the data without transfering data 
	@JsonIgnore
 	@DBRef(lazy=true)
	List<Note> listOfNote = new ArrayList<>();

}
