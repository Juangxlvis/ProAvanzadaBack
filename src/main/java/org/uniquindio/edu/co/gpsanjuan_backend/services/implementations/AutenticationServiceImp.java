package org.uniquindio.edu.co.gpsanjuan_backend.services.implementations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.uniquindio.edu.co.gpsanjuan_backend.DTO.LoginDTO;
import org.uniquindio.edu.co.gpsanjuan_backend.services.interfaces.AutenticationService;
@AllArgsConstructor
@Service
public class AutenticationServiceImp implements AutenticationService {

    private final EntityManager entityManager;

    @Transactional
    @Override
    public Character login(LoginDTO user) {
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("login");

        storedProcedure.registerStoredProcedureParameter("p_id", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("rol", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("res", Character.class, ParameterMode.OUT);

        storedProcedure.setParameter("p_id", user.id());
        storedProcedure.setParameter("rol", user.rol());

        storedProcedure.execute();

        Character r =  (Character) storedProcedure.getOutputParameterValue("res");
        return (r != null) ? r : '0';
    }
}
