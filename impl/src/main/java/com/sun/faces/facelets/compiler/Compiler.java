/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.faces.facelets.compiler;

import com.sun.faces.facelets.tag.CompositeTagDecorator;
import com.sun.faces.facelets.tag.CompositeTagLibrary;
import com.sun.faces.facelets.tag.TagLibrary;
import com.sun.faces.facelets.util.ReflectionUtil;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import javax.el.ExpressionFactory;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.TagDecorator;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Compiler instance may handle compiling multiple sources
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public abstract class Compiler {

    protected final static Logger log = FacesLogger.FACELETS_COMPILER.getLogger();

    public final static String EXPRESSION_FACTORY = "compiler.ExpressionFactory";

    private static final TagLibrary EMPTY_LIBRARY = new CompositeTagLibrary(
            new TagLibrary[0]);

    private static final TagDecorator EMPTY_DECORATOR = new CompositeTagDecorator(
            new TagDecorator[0]);

    private boolean validating = false;

    private boolean trimmingWhitespace = false;

    private boolean trimmingComments = false;

    private final List libraries = new ArrayList();

    private final List decorators = new ArrayList();

    private final Map features = new HashMap();


    /**
     * 
     */
    public Compiler() {
        
    }

    public final FaceletHandler compile(URL src, String alias)
    throws IOException {
        //if (!this.initialized)
        //    this.initialize();
        return this.doCompile(src, alias);
    }

    public final FaceletHandler metadataCompile(URL src, String alias)
    throws IOException {

        return this.doMetadataCompile(src, alias);
    }

    protected abstract FaceletHandler doMetadataCompile(URL src, String alias)
    throws IOException;

    protected abstract FaceletHandler doCompile(URL src, String alias)
    throws IOException;

    public final TagDecorator createTagDecorator() {
        if (this.decorators.size() > 0) {
            return new CompositeTagDecorator((TagDecorator[]) this.decorators
                    .toArray(new TagDecorator[this.decorators.size()]));
        }
        return EMPTY_DECORATOR;
    }

    public final void addTagDecorator(TagDecorator decorator) {
        Util.notNull("decorator", decorator);
        if (!this.decorators.contains(decorator)) {
            this.decorators.add(decorator);
        }
    }

    public final ExpressionFactory createExpressionFactory() {
        ExpressionFactory el = null;
        el = (ExpressionFactory) this.featureInstance(EXPRESSION_FACTORY);
        if (el == null) {
            try {
                el = FacesContext.getCurrentInstance().getApplication()
                        .getExpressionFactory();
                if (el == null) {
                	if (log.isLoggable(Level.WARNING)) {
	                    log.warning("No default ExpressionFactory from Faces Implementation, attempting to load from Feature["
	                                + EXPRESSION_FACTORY + "]");
                	}
                }
            } catch (Exception e) {
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "Unable to get ExpressionFactory because of: ", e);
                }
            }
        }
        if (el == null) {
            this.features.put(EXPRESSION_FACTORY, "com.sun.el.ExpressionFactoryImpl");
            el = (ExpressionFactory) this.featureInstance(EXPRESSION_FACTORY);
        }
        return el;
    }

    private final Object featureInstance(String name) {
        String type = (String) this.features.get(name);
        if (type != null) {
            try {
                return ReflectionUtil.forName(type).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException t) {
                throw new FaceletException("Could not instantiate feature["
                        + name + "]: " + type);
            }
        }
        return null;
    }

    public final TagLibrary createTagLibrary(CompilationMessageHolder unit) {
        if (this.libraries.size() > 0) {
            return new CompositeTagLibrary((TagLibrary[]) this.libraries
                    .toArray(new TagLibrary[this.libraries.size()]), unit);
        }
        return EMPTY_LIBRARY;
    }

    public final void addTagLibrary(TagLibrary library) {
        Util.notNull("library", library);
        if (!this.libraries.contains(library)) {
            this.libraries.add(library);
        }
    }

    public final void setFeature(String name, String value) {
        this.features.put(name, value);
    }

    public final String getFeature(String name) {
        return (String) this.features.get(name);
    }

    public final boolean isTrimmingComments() {
        return this.trimmingComments;
    }

    public final void setTrimmingComments(boolean trimmingComments) {
        this.trimmingComments = trimmingComments;
    }

    public final boolean isTrimmingWhitespace() {
        return this.trimmingWhitespace;
    }

    public final void setTrimmingWhitespace(boolean trimmingWhitespace) {
        this.trimmingWhitespace = trimmingWhitespace;
    }

    public final boolean isValidating() {
        return this.validating;
    }

    public final void setValidating(boolean validating) {
        this.validating = validating;
    }
}
