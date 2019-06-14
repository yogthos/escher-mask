(ns escher-mask.core
  (:require
   [reagent.core :as r]
   ["escher.js" :as escher]))

(def width (/ (.-innerWidth js/window) 2))
(def height (/ (.-innerHeight js/window) 2))

(defn point [viewport event]
  (-> viewport
      .-inverseMatrix
      (.transformPoint (escher/Vector2. (.-clientX event) (.-clientY event)))))

(defn add-box [viewport group black-layer]
  (fn [event]
    (let [box (escher/BoxMask.)]
      (set! (.-invert box) true)
      (set! (.-draggable box) true)
      (-> black-layer .-masks (.push box))
      (.add group box)
      (when event
        (-> box .-position (.copy (point viewport event))))
      (escher/Helpers.boxResizeTool box))))

(defn background []
  (let [background (escher/Image. "img/hexagon.jpg")]
    (set! (.-layer background) -20)
    background))

(defn black-layer []
  (let [object (escher/Object2D.)]
    (set! (.-ignoreViewport object) true)
    (set! (.-layer object) 10)
    (set! (.-draw object)
          (fn [ctx viewport canvas]
            (set! (.-fillStyle ctx) "rgba(0.0, 0.0, 0.0, 0.3)")
            (.fillRect ctx 0 0 width height)))
    (set! (.-isInside object) (fn [point] false))
    object))

(defn init-canvas [canvas viewport group black-layer]
  (set! (.-oncontextmenu canvas)
        (fn [event]
          (.preventDefault event)
          false))
  (set! (.-ondblclick canvas) (add-box viewport group black-layer))
  (.ondblclick canvas))

(defn canvas []
  (r/create-class
   {:component-did-mount (fn [component]
                           (let [canvas      (r/dom-node component)
                                 group       (escher/Object2D.)
                                 black-layer (black-layer)
                                 viewport    (escher/Viewport.)]
                             (.add group (background))
                             (.add group black-layer)
                             (init-canvas canvas viewport group black-layer)
                             (.createRenderLoop (escher/Renderer. canvas) group viewport)))
    :render              (fn [] [:canvas {:width  width :height height}])}))

(defn home-page []
  [:div
   {:style {:overflow :hidden
            :width    width
            :height   height
            :margin   "0 auto"}}
   [canvas]])

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
