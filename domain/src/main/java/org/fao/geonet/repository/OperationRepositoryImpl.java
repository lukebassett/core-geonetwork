package org.fao.geonet.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedOperation;

import java.util.List;


/**
 * Implementation for all {@link Operation} queries that cannot be automatically generated by Spring-data.
 *
 * @author Jesse
 */
public class OperationRepositoryImpl implements OperationRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;
    
    @Override
    public Operation findReservedOperation(ReservedOperation operation) {
        return _entityManager.find(Operation.class, operation.getId());
    }

}