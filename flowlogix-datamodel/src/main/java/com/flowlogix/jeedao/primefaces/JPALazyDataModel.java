/*
 * Copyright (C) 2001-2014, Bett-A-Way, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited unless authorized in writing by Bett-A-Way, Inc.
 */
package com.flowlogix.jeedao.primefaces;

import com.flowlogix.jeedao.primefaces.internal.JPAFacadeLocal;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * Easy implementation of PrimeFaces lazy data model
 * using Lambdas
 * 
 * @author lprimak
 * @param <KK> Key Type
 * @param <TT> Data Type
 */
public class JPALazyDataModel<KK, TT> extends LazyDataModel<TT>
{
    /**
     * Provides Entity Manager
     */
    @FunctionalInterface
    public interface EntityManagerGetter
    {
        /**
         * @return Entity Manager
         */
        EntityManager get();
    }
    
    /**
     * Convert Key String to Key Type
     * 
     * @param <KK> Key Type
     */
    @FunctionalInterface
    public interface KeyConverter<KK>
    {
        /**
         * return the key in the appropriate type
         * 
         * @param keyStr
         * @return key type
         */
        KK convert(String keyStr);
    }
    
    /**
     * Filter Hook
     * 
     * @param <TT> Entity Type
     */
    @FunctionalInterface
    public interface Filter<TT>
    {
        /**
         * filter data
         * this is what you replace with your own filter
         */
        @RequiredArgsConstructor @Getter
        public static class FilterData
        {
            /**
             * filter field value
             */
            private final String fieldValue;
            /**
             * Existing or null predicate, can replace with custom
             */
            private final Predicate predicate;
        }
        
        /**
         * hook to supply custom filter
         * 
         * @param filters user input
         * @param cb
         * @param root 
         */
        void filter(Map<String, FilterData> filters, CriteriaBuilder cb, Root<TT> root);
    }

    /**
     * Sorter Hook
     * @param <TT> Entity Type
     */
    @FunctionalInterface
    public interface Sorter<TT>
    {
        /**
         * Hook for sort criteria application
         * can remove elements from the iterator and do your own action
         * any elements left will be done via the default mechanism
         * 
         * @param sortCriteria
         * @param cb
         * @param root
         * @return iterator to sort data, or, more likely same sortCriteria parameter
         */
        Iterator<SortMeta> sort(Iterator<SortMeta> sortCriteria, CriteriaBuilder cb, Root<TT> root);
    }
    
    /**
     * Hook to add hints to the JPA query
     * 
     * @param <TT> Entity Type
     */
    @FunctionalInterface
    public interface Optimizer<TT>
    {
        /**
         * Add hints to the JPA query
         * Mostly used for batch fetch
         * 
         * @param query to add hints to
         * @return the same query
         */
        TypedQuery<TT> addHints(TypedQuery<TT> query);
    }
    
    
    /**
     * Set up this particular instance of the data model
     * with entity manager, class and key converter
     * 
     * @param emg
     * @param entityClass
     * @param converter 
     */
    public void setup(EntityManagerGetter emg, Class<TT> entityClass, KeyConverter<KK> converter)
    {
        this.emg = emg;
        this.entityClass = entityClass;
        this.converter = converter;
    }


    /**
     * set filter hook
     * 
     * @param filter 
     */
    public void setFilter(Filter<TT> filter)
    {
        this.filter = Optional.of(filter);
    }
    
    
    /**
     * remove filter hook
     */
    public void removeFilter()
    {
        filter = Optional.absent();
    }

    
    /**
     * set sorter hook
     * 
     * @param sorter 
     */
    public void setSorter(Sorter<TT> sorter)
    {
        this.sorter = Optional.of(sorter);
    }
    
    
    /**
     * remove sorter hook
     */
    public void removeSorter()
    {
        sorter = Optional.absent();
    }
    

    /**
     * add hints to JPA query
     * 
     * @param optimizier 
     */
    public void addOptimizerHints(Optimizer<TT> optimizier)
    {
        this.optimizer = Optional.of(optimizier);
    }

    
    /**
     * remove hints from JPA query
     */
    public void removeOptimizerHints()
    {
        this.optimizer = Optional.absent();
    }
    
    
    /**
     * transforms JPA entity field to format suitable for hints
     * 
     * @param val
     * @return JPA field suitable for hints
     */
    public String getResultField(String val)
    {
        return String.format("%s.%s", RESULT, val);
    }
    
    
    @Override
    @SuppressWarnings("unchecked")
    public KK getRowKey(TT entity)
    {
        return (KK)emg.get().getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
    }

    
    @Override
    @Transactional
    public TT getRowData(String rowKey)
    {   
        facade.setup(emg, entityClass, optimizer, filter, sorter);
        return facade.find(converter.convert(rowKey));
    }

    
    @Override
    @Transactional
    public List<TT> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters)
    {
        SortMeta sm = new SortMeta();
        sm.setSortField(sortField);
        sm.setSortOrder(sortOrder);
        return load(first, pageSize, sortField == null? ImmutableList.of() : ImmutableList.of(sm), filters);
    }    

    
    @Override
    public List<TT> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters)
    {
        facade.setup(emg, entityClass, optimizer, filter, sorter);
        setRowCount(facade.count(filters));
        return facade.findRows(first, pageSize, filters, multiSortMeta == null? ImmutableList.of() : multiSortMeta);
    }

    
    private @Inject JPAFacadeLocal<TT, KK> facade;
    private EntityManagerGetter emg;
    private Class<TT> entityClass;
    private KeyConverter<KK> converter;
    private Optional<Filter<TT>> filter = Optional.absent();
    private Optional<Sorter<TT>> sorter = Optional.absent();
    private Optional<Optimizer<TT>> optimizer = Optional.absent();
    public static final String RESULT = "result";

    private static final long serialVersionUID = 1L;
}
