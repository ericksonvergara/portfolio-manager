package com.portafolio_manager.backend.application.port.out.technology;

import com.portafolio_manager.backend.application.port.out.PersistencePort;
import com.portafolio_manager.backend.domain.technology.AdminTechnology;
import com.portafolio_manager.backend.domain.technology.Technology;
import com.portafolio_manager.backend.domain.technology.TechnologyDraft;
import com.portafolio_manager.backend.domain.technology.TechnologyUpdate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TechnologyRepositoryPort extends PersistencePort {

    /** Every technology, by display order and then by name. */
    List<Technology> findAllInDisplayOrder();

    /** The ids of the given set that exist in the catalog. */
    Set<Long> findExistingIds(Set<Long> ids);

    /** Every technology with the number of projects that use it, in display order. */
    List<AdminTechnology> findAllForAdmin();

    Optional<AdminTechnology> findForAdmin(Long id);

    /** Whether a technology other than {@code excludedId} (null: any) has that name, ignoring case. */
    boolean existsByNameIgnoringCase(String name, Long excludedId);

    /** Throws {@code DuplicateTechnologyNameException} if the name was taken concurrently. */
    AdminTechnology create(TechnologyDraft draft);

    /** Empty if it does not exist; throws {@code DuplicateTechnologyNameException} like {@link #create}. */
    Optional<AdminTechnology> update(TechnologyUpdate update);

    /** False if it does not exist; throws {@code TechnologyInUseException} if a project uses it meanwhile. */
    boolean delete(Long id);
}
