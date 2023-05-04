package br.com.senior.erp.man.pcp.programacaocontrole.domain.otif;

import java.time.LocalDate;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import br.com.senior.erp.man.pcp.programacaocontrole.domain.familia.FamiliaRepository;
import br.com.senior.erp.man.pcp.programacaocontrole.domain.op.origem.OrdemProducaoOrigem;
import br.com.senior.erp.man.pcp.programacaocontrole.domain.op.origem.OrdemProducaoOrigemRepository;
import br.com.senior.erp.man.pcp.programacaocontrole.domain.pessoa.PessoaRepository;
import br.com.senior.erp.man.pcp.programacaocontrole.domain.sku.SKURepository;
import br.com.senior.erpman.pcpprogramacaocontrole.EnumSituacaoDemanda;
import br.com.senior.erpman.pcpprogramacaocontrole.RecFilterNeedsOTIF;

@Component
public final class SqlBuilderWhere {

    private static final String AND = " AND ";

    private static final int ANDSIZE = 5;

    @Inject
    private PessoaRepository pessoaRepository;

    @Inject
    private FamiliaRepository familiaRepository;

    @Inject
    private SKURepository skuRepository;

    @Inject
    private OrdemProducaoOrigemRepository opOrigemRepository;

    private SqlBuilderWhere() {
    }

    public String sqlWhereBuilder(RecFilterNeedsOTIF request) {

        var mountSqlWhere = new StringBuilder(" WHERE ");

        mountSqlWhere.append(appendProductionOrderIds(request.productionOrderIds));
        mountSqlWhere.append(appendCompanyCode(request.companyCode));
        mountSqlWhere.append(appendBranchCode(request.branchCode, request.companyCode));
        mountSqlWhere.append(appendFamilyCode(request.familyCode));
        mountSqlWhere.append(appendSkuIds(request.skuIds));
        mountSqlWhere.append(appendDocuments(request.documents));
        mountSqlWhere.append(appendStartDate(request.startDate));
        mountSqlWhere.append(appendEndDate(request.endDate));
        mountSqlWhere.append(appendClient(request.client));
        mountSqlWhere.append(appendDemands(request.demands));
        mountSqlWhere.append(appendSituationDemand(request.situationDemand));
        mountSqlWhere.append(appendDeliveryStartDate(request.deliveryStartDate));
        mountSqlWhere.append(appendDeliveryEndDate(request.deliveryEndDate));

        if (mountSqlWhere.toString().endsWith(AND)) {
            mountSqlWhere.setLength(mountSqlWhere.length() - ANDSIZE);
        }

        return mountSqlWhere.toString();
    }

    private StringBuilder appendProductionOrderIds(List<Long> productionOrderIds) {
        var sb = new StringBuilder();
        if (productionOrderIds != null && !productionOrderIds.isEmpty()) {
            var productionOrderIdsString = "";
            productionOrderIdsString = forProductionOrderIdsValidation(productionOrderIds, productionOrderIdsString);
            productionOrderIdsString = productionOrderIdsString.replaceAll("[\\[\\]]", "");
            sb.append("consolidacao_demanda_remote.id_externo_documento IN(").append(productionOrderIdsString).append(")").append(AND);
        }
        return sb;
    }

    private String forProductionOrderIdsValidation(List<Long> productionOrderIds, String productionOrderIdsString) {
        for (Long opId : productionOrderIds) {
            var opData = opOrigemRepository.findByOrdemProducaoId(opId);
            if ("".equals(productionOrderIdsString)) {
                for (OrdemProducaoOrigem op : opData) {
                    productionOrderIdsString = productionOrderIdsString.concat("'" + op.getCodigoDocumento() + "'");
                }
            } else {
                for (OrdemProducaoOrigem op : opData) {
                    productionOrderIdsString = productionOrderIdsString.concat(", '" + op.getCodigoDocumento() + "'");
                }
            }
        }
        return productionOrderIdsString;
    }

    private StringBuilder appendCompanyCode(Long companyCode) {
        var sb = new StringBuilder();
        if (companyCode != null) {
            var companyUuid = pessoaRepository.findEmpresaByCodigoThrowsException(companyCode);
            sb.append("consolidacao_demanda_remote.empresa_id = '").append(companyUuid.getErpxId()).append("'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendBranchCode(Long branchCode, Long companyCode) {
        var sb = new StringBuilder();
        if (branchCode != null) {
            var filialUuid = pessoaRepository.findFilialByCodigoThrowsException(branchCode, companyCode);
            sb.append("consolidacao_demanda_remote.filial_id = '").append(filialUuid.getErpxId()).append("'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendFamilyCode(String familyCode) {
        var sb = new StringBuilder();
        if (familyCode != null) {
            var familyUuid = familiaRepository.findFamiliaByCodigoThrowsException(familyCode);
            sb.append("e075der_remote.e012fam_id = '").append(familyUuid.getErpxId()).append("'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendSkuIds(List<String> skuIds) {
        var sb = new StringBuilder();
        if (skuIds != null && !skuIds.isEmpty()) {
            var skuIdsConcat = "";
            for (String sku : skuIds) {
                var skuData = skuRepository.findByCodigoThrowsException(sku);
                if ("".equals(skuIdsConcat)) {
                    skuIdsConcat = skuIdsConcat.concat("'" + skuData.getErpxId() + "'");
                } else {
                    skuIdsConcat = skuIdsConcat.concat(", '" + skuData.getErpxId() + "'");
                }
            }
            sb.append("e120ipd_remote.e075der_id IN (").append(skuIdsConcat).append(")").append(AND);
        }
        return sb;
    }

    private StringBuilder appendDocuments(List<Long> documents) {
        var sb = new StringBuilder();
        if (documents != null && !documents.isEmpty()) {
            var documentsConcat = "";
            for (Long d : documents) {
                if ("".equals(documentsConcat)) {
                    documentsConcat = documentsConcat.concat("'%PED-" + d + "%'");
                } else {
                    documentsConcat = documentsConcat.concat(", '%PED-" + d + "%'");
                }
            }
            sb.append("consolidacao_demanda_remote.numero_documento LIKE any(array[").append(documentsConcat).append("])").append(AND);
        }
        return sb;
    }

    private StringBuilder appendStartDate(LocalDate startDate) {
        var sb = new StringBuilder();
        if (startDate != null) {
            sb.append("consolidacao_demanda_remote.data_geracao >= ").append("'" + startDate + "'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendEndDate(LocalDate endDate) {
        var sb = new StringBuilder();
        if (endDate != null) {
            sb.append("consolidacao_demanda_remote.data_geracao <= ").append("'" + endDate + "'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendClient(List<Long> client) {
        var sb = new StringBuilder();
        if (client != null && !client.isEmpty()) {
            var clientsConcat = "";
            for (Long c : client) {
                var dataClient = pessoaRepository.findClienteByCodigoThrowsException(c);
                if ("".equals(clientsConcat)) {
                    clientsConcat = clientsConcat.concat("'" + dataClient.getErpxId() + "'");
                } else {
                    clientsConcat = clientsConcat.concat(", '" + dataClient.getErpxId() + "'");
                }
            }
            sb.append("e120ped_remote.e001pescli_id IN (").append(clientsConcat).append(")").append(AND);
        }
        return sb;
    }

    private StringBuilder appendDemands(List<Long> demands) {
        var sb = new StringBuilder();
        if (demands != null && !demands.isEmpty()) {
            var demandsConcat = "";
            for (Long d : demands) {
                if ("".equals(demandsConcat)) {
                    demandsConcat = demandsConcat.concat("'" + d + "'");
                } else {
                    demandsConcat = demandsConcat.concat(", '" + d + "'");
                }
            }
            sb.append("consolidacao_demanda_remote.demanda_codigo IN (").append(demandsConcat).append(")").append(AND);
        }
        return sb;
    }

    private StringBuilder appendSituationDemand(EnumSituacaoDemanda situationDemand) {
        var sb = new StringBuilder();
        if (situationDemand != null) {
            sb.append("consolidacao_demanda_remote.situacao = ").append("'" + situationDemand + "'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendDeliveryStartDate(LocalDate deliveryStartDate) {
        var sb = new StringBuilder();
        if (deliveryStartDate != null) {
            sb.append("consolidacao_demanda_remote.data_entrega >= ").append("'" + deliveryStartDate + "'").append(AND);
        }
        return sb;
    }

    private StringBuilder appendDeliveryEndDate(LocalDate deliveryEndDate) {
        var sb = new StringBuilder();
        if (deliveryEndDate != null) {
            sb.append("consolidacao_demanda_remote.data_entrega <= ").append("'" + deliveryEndDate + "'").append(AND);
        }
        return sb;
    }

}
