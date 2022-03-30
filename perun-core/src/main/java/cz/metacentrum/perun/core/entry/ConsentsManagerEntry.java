package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.ConsentsManager;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.bl.ConsentsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;

import java.util.List;

/**
 * Consents entry logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ConsentsManagerEntry implements ConsentsManager {

	private ConsentsManagerBl consentsManagerBl;
	private PerunBl perunBl;

	public ConsentsManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.consentsManagerBl = perunBl.getConsentsManagerBl();
	}

	public ConsentsManagerEntry() {}

	public ConsentsManagerBl getConsentsManagerBl() {
		return this.consentsManagerBl;
	}

	public void setConsentsManagerBl(ConsentsManagerBl consentsManagerBl) {
		this.consentsManagerBl = consentsManagerBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	@Override
	public List<ConsentHub> getAllConsentHubs(PerunSession sess) throws PrivilegeException {
		Utils.notNull(sess, "sess");

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllConsentHubs_policy")) {
			throw new PrivilegeException(sess, "getAllConsentHubs");
		}
		return consentsManagerBl.getAllConsentHubs(sess);
	}

	@Override
	public ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException, PrivilegeException {
		Utils.notNull(sess, "sess");

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getConsentHubById_int_policy")) {
			throw new PrivilegeException(sess, "getConsentHubById");
		}

		// Block of code prepared for manage FACILITY ADMIN/OBSERVER roles
		// Don't forget to check roles in perun-roles.yml
		/*ConsentHub consentHub = consentsManagerBl.getConsentHubById(sess, id);
		List<Facility> facilities = consentHub.getFacilities();
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getConsentHub_policy", facility));

		// Authorization
		if (facilities.isEmpty()) {
			throw new PrivilegeException(sess, "getConsentHubById");
		}*/

		return consentsManagerBl.getConsentHubById(sess, id);
	}

	@Override
	public ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException, PrivilegeException {
		Utils.notNull(sess, "sess");
		Utils.notNull(name, "name");

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getConsentHubByName_String_policy")) {
			throw new PrivilegeException(sess, "getConsentHubByName");
		}

		// Block of code prepared for manage FACILITY ADMIN/OBSERVER roles
		// Don't forget to check roles in perun-roles.yml
		/*ConsentHub consentHub = consentsManagerBl.getConsentHubByName(sess, name);
		List<Facility> facilities = consentHub.getFacilities();
		facilities.removeIf(facility -> !AuthzResolver.authorizedInternal(sess, "filter-getConsentHub_policy", facility));

		// Authorization
		if (facilities.isEmpty()) {
			throw new PrivilegeException(sess, "getConsentHubByName");
		}*/

		return consentsManagerBl.getConsentHubByName(sess, name);
	}

	@Override
	public ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException, PrivilegeException, FacilityNotExistsException {
		Utils.notNull(sess, "sess");

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getConsentHubByFacility_Facility_policy", perunBl.getFacilitiesManagerBl().getFacilityById(sess, facilityId))) {
			throw new PrivilegeException(sess, "getConsentHubByFacility");
		}

		return consentsManagerBl.getConsentHubByFacility(sess, facilityId);
	}


	@Override
	public List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		getConsentsManagerBl().checkConsentHubExists(sess, consentsManagerBl.getConsentHubById(id));

		// auth
		if (!AuthzResolver.authorizedInternal(sess, "getConsentsForConsentHub_Id_ConsentStatus_policy")){
			throw new PrivilegeException("getConsentsForConsentHub");
		}

		// TODO maybe filter here?

		return consentsManagerBl.getConsentsForConsentHub(sess, id, status);
	}

	@Override
	public List<Consent> getConsentsForConsentHub(PerunSession sess, int id) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		getConsentsManagerBl().checkConsentHubExists(sess, consentsManagerBl.getConsentHubById(id));

		// auth
		if (!AuthzResolver.authorizedInternal(sess, "getConsentsForConsentHub_Id_policy")){
			throw new PrivilegeException("getConsentsForConsentHub");
		}

		// TODO maybe filter here?

		return consentsManagerBl.getConsentsForConsentHub(sess, id);
	}

	@Override
	public List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status) throws UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getUsersManagerBl().checkUserExists(sess, getPerunBl().getUsersManager().getUserById(sess, id));

		// auth
		if (!AuthzResolver.authorizedInternal(sess, "getConsentsForUser_Id_ConsentStatus_policy")){
			throw new PrivilegeException("getConsentsForUser");
		}

		// TODO maybe filter here?


		return consentsManagerBl.getConsentsForUser(sess, id, status);
	}

	@Override
	public List<Consent> getConsentsForUser(PerunSession sess, int id) throws UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getUsersManagerBl().checkUserExists(sess, getPerunBl().getUsersManager().getUserById(sess, id));

		// auth
		if (!AuthzResolver.authorizedInternal(sess, "getConsentsForUser_Id_policy")){
			throw new PrivilegeException("getConsentsForUser");
		}

		// TODO maybe filter here?


		return consentsManagerBl.getConsentsForUser(sess, id);
	}

	@Override
	public Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// auth
		if (!AuthzResolver.authorizedInternal(sess, "getConsentById_Id_policy")){
			throw new PrivilegeException("getConsentById");
		}

		return consentsManagerBl.getConsentById(sess, id);
	}

}
