package Interfaces;

import java.util.List;

import Beans.DBController;
import Beans.IncidenciaBean;

/**
 * Created by diego on 27/06/2016.
 */
public interface IncidenciasInterface {

    public Void getIncidenciasRemote(List<IncidenciaBean> lstIncidenciasRemote, DBController controller);

    public Void getIncidenciasInsertadas(List<IncidenciaBean> lstIncidenciasRemote, DBController controller);
}