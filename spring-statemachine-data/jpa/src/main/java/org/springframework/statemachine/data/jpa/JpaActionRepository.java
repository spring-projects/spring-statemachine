package org.springframework.statemachine.data.jpa;

import org.springframework.statemachine.data.ActionRepository;

/**
 * A {@link ActionRepository} interface for JPA used for actions.
 *
 * @author Janne Valkealahti
 *
 */
public interface JpaActionRepository extends ActionRepository<JpaRepositoryAction> {
}
