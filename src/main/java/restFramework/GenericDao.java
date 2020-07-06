/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

/*

    Premio p = Premio.query().idPremio(3).findOne();

    int updateCount = Premio
                        .query()
                            .idEmpresa(3)
                        .update()
                            .idade().add(2)
                            .deMaior().customClause("idade > 18")
                            .execute();
                        
    p.save();
    p.update();

    List<Premio> premios = Premio.query(Premio.Fields.NOME)
                                .idEmpresa(3)
                                .find();

    LazyList<Premio> premiosLazy = Premio.query(Premio.Fields.NOME)
                                        .dataInclusao().lessThan(System.currentTimeMillis())
                                        .findLazy();

    
    List<Premio> premiosOrder = Premio.query(Premio.Fields.NOME).idEmpresa(3)
                                        .orderBy()
                                            .nome()
                                        .find();

*/

/**
 *
 * @author Ivo
 */
public class GenericDao extends Dao {
    
}
