package com.cyberintech.vrisk.server.service;

import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnitLevels;
import com.cyberintech.vrisk.server.model.jpa.entity.BusinessUnits;
import com.cyberintech.vrisk.server.repository.jpa.BusinessUnitLevelsRepository;
import com.cyberintech.vrisk.server.repository.jpa.BusinessUnitRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusinessUnitLevelService {

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private BusinessUnitRepository businessUnitRepository;

	@Autowired
	private BusinessUnitLevelsRepository businessUnitLevelsRepository;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Get Business Unit Levels List by child
	 * (Parents of all levels for given Business Unit incl. the level of item itself)
	 *
	 * @return Business Unit Levels List
	 */
	public List<BusinessUnitLevels> getListByChild(Long childId) {
		List<BusinessUnitLevels> items = businessUnitLevelsRepository.findAllByChildId(childId);

		return items;
	}

	/**
	 * Get Business Unit Levels List by parent
	 * (full branch of children for given Business Unit)
	 *
	 * @return Business Unit Levels List
	 */
	public List<BusinessUnitLevels> getListByParent(Long parentId) {
		List<BusinessUnitLevels> items = businessUnitLevelsRepository.findAllByParentIdAndChildIdNot(parentId, parentId);

		return items;
	}

	/**
	 * Rebuild Business Unit Levels for all Business Units
	 * As the result here will be deleted and recreated Business Unit Levels related to all existent Business Units
	 *
	 */
	public List<BusinessUnitLevels> rebuildBusinessUnitLevels() {
		// Get List of top level items (parent_id == null)
//		Long organizationId = organizationService.getCurrentOrganizationId();
//		List<BusinessUnits> topLevelUnits = businessUnitRepository.getAllByParentNullAndOrganizationId(organizationId);

		List<BusinessUnits> topLevelUnits = businessUnitRepository.getAllByParentNull();

		List<BusinessUnitLevels> rebuiltLevels = new ArrayList<>();
		for (BusinessUnits topLevelUnit: topLevelUnits) {
			rebuiltLevels.addAll(this.buildBusinessUnitLevelsBottom(topLevelUnit));
		}
		return rebuiltLevels;
	}

	/**
	 * Build Business Unit Levels for Business Unit and its children
	 *
	 * @param item	Business Unit for which Levels will be created
	 * @return Business Unit Levels List
	 */
	public List<BusinessUnitLevels> buildBusinessUnitLevelsBottom(BusinessUnits item) {
		List<BusinessUnitLevels> result = new ArrayList<>();

		if (item != null && item.getId() != null) {
			// add item's levels
			result.addAll(this.buildBusinessUnitLevels(item.getId()));
			List<BusinessUnits> children = businessUnitRepository.getAllByParentId(item.getId());

			for (BusinessUnits child: children) {
				result.addAll(this.buildBusinessUnitLevelsBottom(child));
			}
		}
		return result;
	}

	public List<BusinessUnitLevels> buildBusinessUnitLevelsBottom(Long businessUnitId) {
		return this.buildBusinessUnitLevelsBottom(businessUnitRepository.findById(businessUnitId).get());
	}

	/**
	 * Build Business Unit Levels for Business Unit and Save them to the database
	 *
	 * @param businessUnitId	id of Business Unit for which Levels will be created
	 * @return Business Unit Levels List
	 */
	public List<BusinessUnitLevels> buildBusinessUnitLevels(Long businessUnitId) {
		BusinessUnits entity = businessUnitRepository.findById(businessUnitId).get();
		// delete all Business Unit Levels where entity presented as child
		this.deleteBusinessUnitLevels(entity.getId());

		// build Business Unit Levels for entity
		BusinessUnits current = this.copyBusinessUnit(entity);

		List<BusinessUnitLevels> itemsToSave = new ArrayList<>();
		while(current != null) {
			BusinessUnitLevels newItem = new BusinessUnitLevels(
				null,
				null, // this field will be ignored while saving entity
				current.getId(), // but we should provide its id
				null, // this field will be ignored while saving entity
				entity.getId(), // but we should provide its id
				null
			);

			itemsToSave.add(newItem);
			current = this.copyBusinessUnit(current.getParent());
		}

		for (int i = 0; i < itemsToSave.size(); i++) {
			BusinessUnitLevels item = itemsToSave.get(i);
			Long level = (long) itemsToSave.size() - i;
			if (level == itemsToSave.size()) level = null;
			item.setLevel(level);
		}

		List<BusinessUnitLevels> saveResult = itemsToSave.stream().map(businessUnitLevel -> businessUnitLevelsRepository.save(businessUnitLevel)).collect(Collectors.toList());

		return saveResult;
	}

	/**
	 * Helper function for copying Business Units (only needed fields)
	 * @param entity
	 * @return
	 */
	private BusinessUnits copyBusinessUnit(BusinessUnits entity) {
		if (entity == null) return null;

		BusinessUnits newItem = new BusinessUnits();
		newItem.setId(entity.getId());
		newItem.setParent(entity.getParent());
		return newItem;
	}

	/**
	 * Delete Business Unit Levels of given Business Unit
	 * (expected to be called only for leaves deletion, other way it's children will still
	 * left with the Level of this Business Unit)
	 *
	 * @param businessUnitId	id of Business Unit which Business Unit Levels should be deleted
	 * @return itemId
	 */
	public Long deleteBusinessUnitLevels(Long businessUnitId) {
		List<BusinessUnitLevels> itemsToDelete = this.getListByChild(businessUnitId);

		if (itemsToDelete.size() > 0) {
			businessUnitLevelsRepository.deleteAll(itemsToDelete);
			businessUnitLevelsRepository.flush();
		}

		return businessUnitId;
	}
}
