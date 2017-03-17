package org.thrx.challenger.one.user.api;

import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

/**
 * An User entity.
 * Der User ist der Teilnehmer an der C.one Plattform. 
 * Der User benötigt dabei einen Account welchen er durch eine erfolgreiche Registrierung bekommt. 
 * Diese Registrierung kann der User beim Start der App/Portal anlegen.
 */

@Value
@Builder
@Wither
public final class User {

	private final UUID id;
    private final String username;
    private final String nickName;
    private final String givenName;
    private final String familyname;
    private final String postCode;
    private final String eMail;
    private final String mobilPhone;
    private final String street;
    private final String city;

    @JsonCreator
    private User(Optional<UUID> id 
	    		,String username
	    		,String nickName
	    		,String givenName
	    		,String familyname
	    		,String postCode
	    		,String eMail
	    		,String mobilPhone
	    		,String street
	    		,String city
    		     ) {
        this.id = id.orElse(null);
		this.username = username;
		this.nickName = nickName;
		this.givenName = givenName;
		this.familyname = familyname;
		this.postCode = postCode;
		this.eMail = eMail;
		this.mobilPhone = mobilPhone;
		this.street = street;
		this.city = city;
    }
	
    public User(UUID id
    		, String username
    		, String nickName
    		, String givenName
    		, String familyname
    		, String postCode
    		, String eMail
    		, String mobilPhone
    		, String street
    		, String city
    		) {
		this.id = id;
		this.username = username;
		this.nickName = nickName;
		this.givenName = givenName;
		this.familyname = familyname;
		this.postCode = postCode;
		this.eMail = eMail;
		this.mobilPhone = mobilPhone;
		this.street = street;
		this.city = city;
	}

    /**
     * Used when creating a new user.
     */
    public User(String username) {
        this.id = null;
        this.username = username;
		this.nickName = null;
		this.givenName = null;
		this.familyname = null;
		this.postCode = null;
		this.eMail = null;
		this.mobilPhone = null;
		this.street = null;
		this.city = null;
    }
}
