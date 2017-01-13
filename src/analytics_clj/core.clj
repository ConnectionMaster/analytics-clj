(ns analytics-clj.core
  (:refer-clojure :exclude [alias flush])
  (:require [analytics-clj.utils :refer [string-keys]])
  (:import (com.segment.analytics Analytics)
           (com.segment.analytics.messages IdentifyMessage
                                           MessageBuilder
                                           TrackMessage)))

(def context {"library" "analytics-clj"})

(defn initialize
  "Start building an Analytics instance."
  [write-key]
  (.build (Analytics/builder write-key)))

(defn enqueue
  "Top-level `enqueue` function to allow for extensibility in the future."
  [^Analytics analytics ^MessageBuilder message]
  (.enqueue analytics message))

(defn flush
  "Flush events in the message queue."
  [^Analytics analytics]
  (.flush analytics))

(defn shutdown
  "Stops this instance from processing further requests."
  [^Analytics analytics]
  (.shutdown analytics))

(defn- enable-integration* [^MessageBuilder message-builder k v]
  (doto message-builder
    (.enableIntegration k v)))

(defn- enable-integrations [^MessageBuilder message-builder integrations]
  (doseq [[k v] integrations]
    (enable-integration* message-builder k v)))

(defn- integration-options* [^MessageBuilder message-builder integration options]
  (.integrationOptions message-builder integration options))

(defn- enable-integration-options [^MessageBuilder message-builder integration-options]
  (doseq [[integration options] integration-options]
    (integration-options* message-builder integration (string-keys options))))

(defn common-properties
  "The `MessageBuilder` interface has a set of properties common to all messages."
  [^MessageBuilder message-builder {:keys [anonymous-id context integration-options integrations timestamp user-id]}]
  (doto message-builder
    (cond-> (not (nil? anonymous-id)) (.anonymousId anonymous-id))
    (cond-> (not (nil? context)) (.context (string-keys context)))
    (cond-> (not (nil? integration-options)) (enable-integration-options integration-options))
    (cond-> (not (nil? integrations)) (enable-integrations integrations))
    (cond-> (not (nil? timestamp)) (.timestamp timestamp))
    (cond-> (not (nil? user-id)) (.userId user-id))))

(defn- traits* [^MessageBuilder message-builder traits]
  (doto message-builder
    (.traits traits)))

(defn identify
  "`identify` lets you tie a user to their actions and
  record traits about them. It includes a unique User ID
  and any optional traits you know about them."
  ([^Analytics analytics user-id]
   (identify analytics user-id {}))
  ([^Analytics analytics user-id traits]
   (identify analytics user-id traits {}))
  ([^Analytics analytics user-id traits options]
   (enqueue analytics (doto (IdentifyMessage/builder)
                        (common-properties (merge {:user-id user-id} options))
                        (cond-> (not (nil? traits)) (traits* (string-keys traits)))))))

(defn- properties* [^MessageBuilder message-builder properties]
  (doto message-builder
    (.properties properties)))

(defn track
  "`track` lets you record the actions your users perform.
  Every action triggers what we call an “event”, which can
  also have associated properties."
  ([^Analytics analytics user-id event]
   (track analytics user-id event {}))
  ([^Analytics analytics user-id event properties]
   (track analytics user-id event properties {}))
  ([^Analytics analytics user-id event properties options]
   (enqueue analytics (doto (TrackMessage/builder event)
                        (common-properties (merge {:user-id user-id} options))
                        (cond-> (not (nil? properties)) (properties* (string-keys properties)))))))

(defn screen
  "The `screen` method lets you you record whenever a user
  sees a screen of your mobile app, along with optional
  extra information about the page being viewed."
  ([^Analytics analytics user-id name category]
   (screen analytics user-id name category {}))
  ([^Analytics analytics user-id name category properties]
   ;; TODO
   ))

(defn group
  "`group` lets you associate an identified user user with
  a group. A group could be a company, organization, account,
  project or team! It also lets you record custom traits
  about the group, like industry or number of employees."
  ([^Analytics analytics user-id group-id]
   (group analytics user-id group-id {}))
  ([^Analytics analytics user-id group-id traits]
   ;; TODO
   ))

(defn alias
  "`alias` is how you associate one identity with another.
  This is an advanced method, but it is required to manage
  user identities successfully in some of our integrations."
  [^Analytics analytics previous-id user-id]
  ;; TODO
  )
