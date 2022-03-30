package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Consent;
import cz.metacentrum.perun.core.api.ConsentHub;
import cz.metacentrum.perun.core.api.ConsentStatus;
import cz.metacentrum.perun.core.api.exceptions.ConsentNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.implApi.ConsentsManagerImplApi;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubAlreadyRemovedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Consents database logic.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ConsentsManagerImpl implements ConsentsManagerImplApi {

	final static Logger log = LoggerFactory.getLogger(ConsentsManagerImpl.class);

	private final static String consentMappingSelectQuery = " consents.id as consents_id, consents.user_id as consents_user_id, consents.consent_hub_id as consents_consent_hub_id, " +
		"consents.status as consents_status, consents.created_at as consents_created_at, consents.created_by as consents_created_by, consents.modified_by as consents_modified_by, consents.modified_at as consents_modified_at, " +
		"consents.modified_by_uid as consents_modified_by_uid, consents.created_by_uid as consents_created_by_uid ";

	private final static String consentHubMappingQuery = "";

	private final static RowMapper<ConsentHub> CONSENT_HUB_MAPPER = (rs, rowNum) -> {
		return new ConsentHub();
	};

	// Consent mapper
	private final static RowMapper<Consent> CONSENT_MAPPER = (rs, rowNum) -> {
		Consent c = new Consent();
		c.setId(rs.getInt("consents_id"));
		c.setUserId(rs.getInt("consents_user_id"));
		c.setConsentHub(CONSENT_HUB_MAPPER.mapRow(rs, rowNum));
		c.setStatus(ConsentStatus.valueOf(rs.getString("consents_status")));
		c.setCreatedAt(rs.getString("consents_created_at"));
		c.setCreatedBy(rs.getString("consents_created_by"));
		c.setModifiedAt(rs.getString("consents_modified_at"));
		c.setModifiedBy(rs.getString("consents_modified_by"));
		if(rs.getInt("consents_modified_by_uid") == 0) c.setModifiedByUid(null);
		else c.setModifiedByUid(rs.getInt("consents_modified_by_uid"));
		if(rs.getInt("consents_created_by_uid") == 0) c.setCreatedByUid(null);
		else c.setCreatedByUid(rs.getInt("consents_created_by_uid"));
		return c;
	};

	private static JdbcPerunTemplate jdbc;

	protected final static String consentHubMappingSelectQuery = "consent_hubs.id as consent_hubs_id, consent_hubs.name as consent_hubs_name, consent_hubs.enforce_consents as consent_hubs_enforce_consents, " +
		"consent_hubs.created_at as consent_hubs_created_at, consent_hubs.created_by as consent_hubs_created_by, consent_hubs.modified_at as consent_hubs_modified_at, " +
		"consent_hubs.modified_by as consent_hubs_modified_by, consent_hubs.created_by_uid as consent_hubs_created_by_uid, consent_hubs.modified_by_uid as consent_hubs_modified_by_uid";

	protected static final RowMapper<ConsentHub> CONSENT_HUB_MAPPER = (resultSet, i) -> {
		ConsentHub consentHub = new ConsentHub();
		consentHub.setId(resultSet.getInt("consent_hubs_id"));
		consentHub.setName(resultSet.getString("consent_hubs_name"));
		consentHub.setEnforceConsents(resultSet.getBoolean("consent_hubs_enforce_consents"));
		consentHub.setCreatedAt(resultSet.getString("consent_hubs_created_at"));
		consentHub.setCreatedBy(resultSet.getString("consent_hubs_created_by"));
		consentHub.setModifiedAt(resultSet.getString("consent_hubs_modified_at"));
		consentHub.setModifiedBy(resultSet.getString("consent_hubs_modified_by"));
		if(resultSet.getInt("consent_hubs_created_by_uid") == 0) consentHub.setCreatedByUid(null);
		else consentHub.setCreatedByUid(resultSet.getInt("consent_hubs_created_by_uid"));
		if(resultSet.getInt("consent_hubs_modified_by_uid") == 0) consentHub.setModifiedByUid(null);
		else consentHub.setModifiedByUid(resultSet.getInt("consent_hubs_modified_by_uid"));
		return consentHub;
	};

	public ConsentsManagerImpl(DataSource perunPool) {
		jdbc = new JdbcPerunTemplate(perunPool);
		jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	@Override
	public List<ConsentHub> getAllConsentHubs(PerunSession sess) {
		try {
			List<ConsentHub> consentHubs = jdbc.query("select " + consentHubMappingSelectQuery + " from consent_hubs", CONSENT_HUB_MAPPER);
			for(ConsentHub consentHub : consentHubs) {
				consentHub.setFacilities(getFacilitiesForConsentHub(consentHub));
			}
			return consentHubs;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public ConsentHub getConsentHubById(PerunSession sess, int id) throws ConsentHubNotExistsException {
		try {
			ConsentHub consentHub = jdbc.queryForObject("select " + consentHubMappingSelectQuery + " from consent_hubs where id=?", CONSENT_HUB_MAPPER, id);
			consentHub.setFacilities(getFacilitiesForConsentHub(consentHub));
			return consentHub;
		} catch (EmptyResultDataAccessException ex) {
			throw new ConsentHubNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public ConsentHub getConsentHubByName(PerunSession sess, String name) throws ConsentHubNotExistsException {
		try {
			ConsentHub consentHub =  jdbc.queryForObject("select " + consentHubMappingSelectQuery + " from consent_hubs where name=?", CONSENT_HUB_MAPPER, name);
			consentHub.setFacilities(getFacilitiesForConsentHub(consentHub));
			return consentHub;
		} catch (EmptyResultDataAccessException ex) {
			throw new ConsentHubNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Facility> getFacilitiesForConsentHub(ConsentHub consentHub) {
		try {
			return jdbc.query("select " + FacilitiesManagerImpl.facilityMappingSelectQuery + " from facilities join consent_hubs_facilities on facilities.id=consent_hubs_facilities.facility_id" +
				" where consent_hubs_facilities.consent_hub_id=?", FacilitiesManagerImpl.FACILITY_MAPPER, consentHub.getId());
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public ConsentHub getConsentHubByFacility(PerunSession sess, int facilityId) throws ConsentHubNotExistsException {
		try {
			ConsentHub consentHub = jdbc.queryForObject("select " + consentHubMappingSelectQuery + " from consent_hubs join consent_hubs_facilities on consent_hubs.id=consent_hubs_facilities.consent_hub_id where consent_hubs_facilities.facility_id=?", CONSENT_HUB_MAPPER, facilityId);
			consentHub.setFacilities(getFacilitiesForConsentHub(consentHub));
			return consentHub;
		} catch (EmptyResultDataAccessException ex) {
			throw new ConsentHubNotExistsException(ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public boolean consentHubExists(PerunSession sess, ConsentHub consentHub) {
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from consent_hubs where id=?", consentHub.getId());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Consent hub " + consentHub + " exists more than once.");
			}
			return false;
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void deleteConsentHub(PerunSession perunSession, ConsentHub consentHub) throws ConsentHubAlreadyRemovedException {
		//TODO: remove all user consents first

		try {
			jdbc.update("delete from consent_hubs_facilities where consent_hub_id=?", consentHub.getId());

			int numAffected = jdbc.update("delete from consent_hubs where id=?", consentHub.getId());
			if (numAffected == 0) throw new ConsentHubAlreadyRemovedException("ConsentHub: " + consentHub);
			log.info("ConsentHub deleted: {}", consentHub);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public ConsentHub createConsentHub(PerunSession sess, ConsentHub consentHub) {
		try {
			int id = Utils.getNewId(jdbc, "consent_hubs_id_seq");
			// if name not set, use facility name
			if (consentHub.getName() == null) {
				consentHub.setName(consentHub.getFacilities().get(0).getName());
			}

			jdbc.update("insert into consent_hubs(id,name,enforce_consents,created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
					"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", id, consentHub.getName(),
				consentHub.isEnforceConsents(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			for (Facility facility : consentHub.getFacilities()) {
				jdbc.update("insert into consent_hubs_facilities(consent_hub_id,facility_id,created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
						"values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", id, facility.getId(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			}
			log.info("ConsentHub created: {}", consentHub);

			consentHub.setId(id);
			return consentHub;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkConsentHubExists(PerunSession sess, ConsentHub consentHub) throws ConsentHubNotExistsException {
		if(!consentHubExists(sess, consentHub)) throw new ConsentHubNotExistsException("ConsentHub not exists: " + consentHub);
	}

	@Override
	public List<Consent> getConsentsForConsentHub(PerunSession sess, int id, ConsentStatus status) {
		try {
			List<Consent> consents = jdbc.query("select " + consentMappingSelectQuery + ", " + consentHubMappingQuery +
				" from consents left join consent_hubs on consents.consent_hub_id=consent_hubs.id " + " where consents.consent_hub_id=? and consents.status=? ", CONSENT_MAPPER, id, status);
			for (Consent consent : consents) {
				if (consent != null) {
					consent.setAttributes(getAttrDefsForConsent(sess, id));
				}
			}
			return consents;
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Consent> getConsentsForConsentHub(PerunSession sess, int id) {
		try {
			List<Consent> consents = jdbc.query("select " + consentMappingSelectQuery + ", " + consentHubMappingQuery +
				" from consents left join consent_hubs on consents.consent_hub_id=consent_hubs.id " + " where consents.consent_hub_id=? ", CONSENT_MAPPER, id);
			for (Consent consent : consents) {
				if (consent != null) {
					consent.setAttributes(getAttrDefsForConsent(sess, id));
				}
			}
			return consents;
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Consent> getConsentsForUser(PerunSession sess, int id, ConsentStatus status) {
		try {
			List<Consent> consents = jdbc.query("select " + consentMappingSelectQuery + ", " + consentHubMappingQuery +
				" from consents left join consent_hubs on consents.consent_hub_id=consent_hubs.id " + " where consents.user_id=? and consents.status=? ", CONSENT_MAPPER, id, status);
			for (Consent consent : consents) {
				if (consent != null) {
					consent.setAttributes(getAttrDefsForConsent(sess, id));
				}
			}
			return consents;
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public List<Consent> getConsentsForUser(PerunSession sess, int id) {
		try {
			List<Consent> consents = jdbc.query("select " + consentMappingSelectQuery + ", " + consentHubMappingQuery +
				" from consents left join consent_hubs on consents.consent_hub_id=consent_hubs.id " + " where consents.user_id=? ", CONSENT_MAPPER, id);
			for (Consent consent : consents) {
				if (consent != null) {
					consent.setAttributes(getAttrDefsForConsent(sess, id));
				}
			}
			return consents;
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public Consent getConsentById(PerunSession sess, int id) throws ConsentNotExistsException {
		try {
			Consent consent = jdbc.queryForObject("select " + consentMappingSelectQuery + ", " + consentHubMappingQuery +
				" from consents left join consent_hubs on consents.consent_hub_id=consent_hubs.id " + " where consents.id=? ", CONSENT_MAPPER, id);
			if (consent != null) {
				consent.setAttributes(getAttrDefsForConsent(sess, id));
			}
			return consent;
		} catch (EmptyResultDataAccessException ex) {
			throw new ConsentNotExistsException(ex);
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}

	@Override
	public void checkConsentExists(PerunSession sess,  Consent consent) throws ConsentNotExistsException {
		if (!consentExists(sess, consent)) throw new ConsentNotExistsException("Consent: " + consent);
	}

	@Override
	public boolean consentExists(PerunSession sess, Consent consent) {
		Utils.notNull(consent, "consent");
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from consents where id=?", consent.getId());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("Consent " + consent + " exists more than once.");
			}
			return false;
		} catch (EmptyResultDataAccessException ex) {
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	private List<AttributeDefinition> getAttrDefsForConsent(PerunSession sess, int consentId) {
		try {
			return jdbc.query("select " + AttributesManagerImpl.attributeDefinitionMappingSelectQuery + " from attr_names join consent_attr_defs on attr_names.id = consent_attr_defs.attr_id" +
				" where consent_attr_defs.consent_id=?", AttributesManagerImpl.ATTRIBUTE_DEFINITION_MAPPER, consentId);
		} catch (EmptyResultDataAccessException ex) {
			return new ArrayList<>();
		} catch (RuntimeException err) {
			throw new InternalErrorException(err);
		}
	}


}
