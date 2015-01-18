/*
 * Copyright 2015 lprimak.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowlogix.jeedao.primefaces.internal;

import com.flowlogix.jeedao.primefaces.interfaces.EntityManagerGetter;
import com.flowlogix.jeedao.primefaces.interfaces.Filter;
import com.flowlogix.jeedao.primefaces.interfaces.Optimizer;
import com.flowlogix.jeedao.primefaces.interfaces.Sorter;
import com.google.common.base.Optional;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.SortMeta;

/**
 * Typed state, as @TransactionScoped beans cannot be typed
 * 
 * @author lprimak
 * @param <TT>
 */
public @Getter @Setter class JPAFacadeTypedState<TT>
{
    private EntityManagerGetter emg;
    private Class<TT> entityClass;
    private Optional<Optimizer<TT>> optimizer;
    private Optional<Filter<TT>> filterHook;
    private Optional<Sorter<TT>> sorterHook;
    private Map<String, Object> filters;
    private List<SortMeta> sortMeta;
}
