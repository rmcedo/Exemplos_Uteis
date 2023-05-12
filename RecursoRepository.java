@Repository
public class RecursoRepository extends AbstractRepository<Recurso, QRecurso> {

    @Inject
    private TranslationHubApi translationHubApi;

    @Override
    protected QRecurso getEntityPath() {
        return QRecurso.recurso;
    }

    public Recurso findByIdThrowsException(Long id) {
        BooleanExpression predicate = getEntityPath().id.eq(id);
        return findOneThrowsException(predicate);
    }

    public Long getQuantidadeRecursosByCentroCusto(long centroCustoId) {
        return count(getEntityPath().centroCusto.id.eq(centroCustoId).and(getEntityPath().ativo.isTrue()));
    }

    public Recurso findById(Long id) {
        BooleanExpression predicate = getEntityPath().id.eq(id);
        return findOne(predicate);
    }

    public boolean isResourceInInformedCompany(Long resourceId, String resourceCode, Long companyId) {
        return findRecursoByIdOrCodeAndEmpresa(resourceId, resourceCode, companyId) != null;
    }

    public Recurso findRecursoByIdOrCodeAndEmpresa(Long resourceId, String resourceCode, Long companyId) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(getEntityPath().pessoa.id.eq(companyId));

        if (resourceId != null) {
            predicate.and(getEntityPath().id.eq(resourceId));
        } else {
            predicate.and(getEntityPath().codigo.eq(resourceCode));
        }

        return findOne(predicate);
    }
/**
 * Query getPredicatePesquisa. O retorno da pesquisa será uma lista de recursos de acordo com o filtro passado pelo usuário no parametro request
 * 
 * @param request
 * @author: rmcedo
 */
    public Predicate getPredicatePesquisa(final PesquisarRecursoInput request) {
        final BooleanBuilder predicate = new BooleanBuilder();

        /**
         * request.empresaCodigo. Realiza a verificação se o código passado no parametro se encontra no DB e se o tipo que consta é do TipoPessoa.EMPRESA
         * 
         * request.empresaCodigo é o único campo não opcional da request
         * 
         * Necessário essa verificação pois na tabela Pessoa, são cadastrados todos os tipos, como EMPRESA, FILIAL, CLIENTE OU FORNECEDOR
         */
        predicate //
                .and(getEntityPath().pessoa.codigo.eq(request.empresaCodigo) //
                        .and(getEntityPath().pessoa.tipo.eq(TipoPessoa.EMPRESA)));


        /**
         * request.filiarCodigo. Como é um campo opcional, primeiro é verificado se o usuário passou o valor para filialCodigo
         * Em seguida é realizada a mesma verificação de empresaCodigo, para verificar se o parametro passado é do TipoPessoa.FILIAL
         */
        if (request.filialCodigo != null) {
            predicate//
                    .and(getEntityPath().filial.codigo.eq(request.filialCodigo)//
                            .and(getEntityPath().filial.tipo.eq(TipoPessoa.FILIAL)));
        }
        /**
         * request.filtro. Como é um campo opcional, primeiramente verificamos se o valor é passado. 
         * Caso seja passado, realizamos a verificação se o parametro passado é do tipo codigo, se sim é realizado um Like na query para verificar a similaridade
         * ou então é verificado se o parametro passado é do tipo descricao, se sim, também é realizado um Like na query para verificar a similaridade
         */
        if (request.filtro != null) {
            predicate.and(getEntityPath().codigo.likeIgnoreCase("%" + request.filtro + "%")//
                    .or(getEntityPath().descricao.likeIgnoreCase("%" + request.filtro + "%")));
        }

        processoIndustrialRecursoSubQuery(request, predicate);
        centroRecursoValidationsAndQueries(request, predicate);

        return predicate;
    }


    /**
     * Nesse método privado, chamado acima, realizamos uma subQuery em outra tabela para buscar o codigo do Recurso de Processos Industriais.
     * Realizamos o select e reutilizamos a saída da query como filtro no parametro request.processoIndustrialCodigo
     * 
     * @param request
     * @param predicate
     */
    private void processoIndustrialRecursoSubQuery(final PesquisarRecursoInput request, final BooleanBuilder predicate) {
        if (request.processoIndustrialCodigo != null) {

            JPAQuery<Long> subQuery = new JPAQuery<>(em);
            subQuery //
                    .select(processoIndustrialRecurso.recursoId.id) //
                    .from(processoIndustrialRecurso) //
                    .where(processoIndustrialRecurso.processoIndId.codigo.toUpperCase()//
                            .eq(request.processoIndustrialCodigo.toUpperCase()));

            predicate.and(getEntityPath().id.in(subQuery.fetch()));

        }
    }
    /**
     * Nesse método privado, chamado acima, realizamos as verificação nos parametros relacionados ao Centro de Recurso.
     * 
     * Realizamos a verificação se o parametro centroRecursoId é passado, se sim, adicionamos o campo dentro do filtro na query
     * 
     * Também é realizado a verificação do request.tipoRecurso. Verificamos se é nulo ou vazio, pois é um campo do tipo Lista.
     * Se sim, adicionamos cada campo, através de um stream().map(), cada campo do filtro na query
     * 
     * @param request
     * @param predicate
     */
    private void centroRecursoValidationsAndQueries(final PesquisarRecursoInput request, final BooleanBuilder predicate) {
        if (request.centroRecursoId != null) {
            predicate.and(getEntityPath().centroRecurso.id.eq(request.centroRecursoId));
        }
        
        if (request.tipoRecurso != null && !request.tipoRecurso.isEmpty()) {
            
            predicate.and(getEntityPath().centroRecurso.tipoRecurso.in(//
                                                                       request.tipoRecurso.stream()//
                                                                       .map(this::mapToTipoRecurso)//
                                                                       .collect(Collectors.toList())));
        }
    }

    /**
     * Nesse método privado, chamado no método privado acima, realizamos o switch dos itens que poderiam ser passados na lista
     * Realizamos uma espécie de conversão do campo. Caso seja do tipo correto, é realizado a "conversão".
     * Caso não seja do tipo desejado, jogamos uma exceção informando que o tipo de recurso é inválido
     */
    private TipoRecurso mapToTipoRecurso(EnumTipoRecurso enumTipoRecurso) {
        switch (enumTipoRecurso) {
            case Celula:
                return TipoRecurso.CELULA_PRODUCAO;
            case Equipamento:
                return TipoRecurso.EQUIPAMENTO;
            case Terceiro:
                return TipoRecurso.RECURSO_TERCEIRO;
            default:
                throw new ServiceException(ErrorCategory.BAD_REQUEST,//
                                           translationHubApi.getMessage(TranslationConstants.TIPO_RECURSO_INVALIDO));
        }
    }

}