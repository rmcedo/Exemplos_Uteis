package br.com.senior.erp.man.pcp.programacaocontrole.domain.otif;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import br.com.senior.erp.man.pcp.database.fdw.DatacenterFilter;
import br.com.senior.erp.man.pcp.database.fdw.FdwExecutor;
import br.com.senior.erpman.pcpprogramacaocontrole.RecFilterNeedsOTIF;
import br.com.senior.erpman.pcpprogramacaocontrole.SearchCalculationOTIFOutput;
import br.com.senior.erpx.dataproxy.model.MetaDataConfig;

@Service
public class BuscaCalculoOTIFService implements FdwExecutor {

    @Inject
    private SqlBuilderWhere where;
    
    private final static String COUNT = "select count(*) ";

    @Override
    public SearchCalculationOTIFOutput execute(MetaDataConfig metaDataConfig, DatacenterFilter filter) throws SQLException {
        FilterCalculationOTIFDTO filter2 = (FilterCalculationOTIFDTO) filter;
        RecFilterNeedsOTIF filtro = (FilterCalculationOTIFDTOConverter.converteDTOParaRecord(filter2));
        return buscaCalculoOTIF(filtro, metaDataConfig);
    }

    private SearchCalculationOTIFOutput buscaCalculoOTIF(RecFilterNeedsOTIF filtro, MetaDataConfig metaDataConfig) throws SQLException {
        var connection = metaDataConfig.getConnection();
        return new SearchCalculationOTIFOutput(totalDeliveryOnTime(filtro, connection), totalDeliveryQuantity(filtro, connection), totalRecords(filtro, connection));
    }

    /**
    * Método selectTotalDeliveryOnTime: temos um método que retornara um select que irá realizar a contagem dos resultados da query.
    * É realizado a concatenação com o FROM do método 'fromBuilder' e a situação da qual desejamos realizar a contagem
    *
    * @author: rmcedo
    */
    private String selectTotalDeliveryOnTime(RecFilterNeedsOTIF filtro) {

        return COUNT + fromBuilder(filtro)+ " and consolidacao_demanda_remote.data_finalizacao_reservas <= consolidacao_demanda_remote.data_entrega;";
    }

    /**
    * Método selectTotalDeliveryQuantity: temos um método que retornara um select que irá realizar a contagem dos resultados da query.
    * É realizado a concatenação com o FROM do método 'fromBuilder' e a situação da qual desejamos realizar a contagem
    *
    * @author: rmcedo
    */
    private String  selectTotalDeliveryQuantity(RecFilterNeedsOTIF filtro) {

        return COUNT + fromBuilder(filtro)+ " and consolidacao_demanda_remote.quantidade_planejada <= consolidacao_demanda_remote.quantidade_reservada;";

    }

    /**
    * Método selectTotalRecords: temos um método que retornara um select que irá realizar a contagem de todos os resultados da query.
    * É realizado a concatenação com o FROM do método 'fromBuilder'. 
    *
    * @author: rmcedo
    */
    private String selectTotalRecords(RecFilterNeedsOTIF filtro) {
        return COUNT + fromBuilder(filtro);
    }

    /**
    * Método fromBuilder foi criado para montagem do FROM que serão utilizados nos SELECTS. Criamos como método para que assim ele seja reutilizado mais de uma vez.
    * a classe SqlWhereBuilder é chamada nesse método, realizando a concatenação com o where montado a partir dos parametros passados na  requisição.
    *
    * @author: rmcedo
    */
    private String fromBuilder(RecFilterNeedsOTIF filtro) {

        return "from consolidacao_demanda_remote" + // 
               " inner join e075der_remote on e075der_remote.e070emp_id = consolidacao_demanda_remote.empresa_id" + //
               " inner join e120ipd_remote on e120ipd_remote.e075der_id = e075der_remote.id" + //
               " inner join e120ped_remote on e120ped_remote.e070emp_id = consolidacao_demanda_remote.empresa_id" + //
               " inner join e012fam_remote on e012fam_remote.e070emp_id = consolidacao_demanda_remote.empresa_id" + where.sqlWhereBuilder(filtro);
    }

    private Double totalDeliveryOnTime(RecFilterNeedsOTIF filtro, Connection connection) throws SQLException {
        Double totalDOT;
        String sql = selectTotalDeliveryOnTime(filtro);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                totalDOT = rs.getDouble(1);
            } else {
                totalDOT = 0D;
            }
        }
        return totalDOT;
    }

    private Double totalDeliveryQuantity(RecFilterNeedsOTIF filtro, Connection connection) throws SQLException {
        Double totalDQ;
        String sql = selectTotalDeliveryQuantity(filtro);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                totalDQ = rs.getDouble(1);
            } else {
                totalDQ = 0D;
            }

        }
        return totalDQ;
    }

    private Long totalRecords(RecFilterNeedsOTIF filtro, Connection connection) throws SQLException {
        Long totalRecords;
        String sql = selectTotalRecords(filtro);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                totalRecords = rs.getLong(1);
            } else {
                totalRecords = 0L;
            }
        }
        return totalRecords;
    }

}
