return {
    beginWork: function(current, workInProgress, renderExpirationTime) {
      if (
        0 === workInProgress.expirationTime ||
        workInProgress.expirationTime > renderExpirationTime
      ) {
        switch (workInProgress.tag) {
          case 3:
            pushHostRootContext(workInProgress);
            break;
          case 2:
            pushLegacyContextProvider(workInProgress);
            break;
          case 4:
            pushHostContainer(
              workInProgress,
              workInProgress.stateNode.containerInfo
            );
            break;
          case 13:
            pushProvider(workInProgress);
        }
        return null;
      }
      switch (workInProgress.tag) {
        case 0:
          invariant(
            null === current,
            "An indeterminate component should never have mounted. This error is likely caused by a bug in React. Please file an issue."
          );
          var fn = workInProgress.type,
            props = workInProgress.pendingProps,
            unmaskedContext = getUnmaskedContext(workInProgress);
          unmaskedContext = getMaskedContext(workInProgress, unmaskedContext);
          fn = fn(props, unmaskedContext);
          workInProgress.effectTag |= 1;
          "object" === typeof fn &&
          null !== fn &&
          "function" === typeof fn.render &&
          void 0 === fn.$$typeof
            ? ((unmaskedContext = workInProgress.type),
              (workInProgress.tag = 2),
              (workInProgress.memoizedState =
                null !== fn.state && void 0 !== fn.state ? fn.state : null),
              "function" === typeof unmaskedContext.getDerivedStateFromProps &&
                ((props = callGetDerivedStateFromProps(
                  workInProgress,
                  fn,
                  props,
                  workInProgress.memoizedState
                )),
                null !== props &&
                  void 0 !== props &&
                  (workInProgress.memoizedState = Object.assign(
                    {},
                    workInProgress.memoizedState,
                    props
                  ))),
              (props = pushLegacyContextProvider(workInProgress)),
              adoptClassInstance(workInProgress, fn),
              mountClassInstance(workInProgress, renderExpirationTime),
              (current = finishClassComponent(
                current,
                workInProgress,
                !0,
                props,
                !1,
                renderExpirationTime
              )))
            : ((workInProgress.tag = 1),
              reconcileChildren(current, workInProgress, fn),
              (workInProgress.memoizedProps = props),
              (current = workInProgress.child));
          return current;
        case 1:
          return (
            (props = workInProgress.type),
            (renderExpirationTime = workInProgress.pendingProps),
            hasLegacyContextChanged() ||
            workInProgress.memoizedProps !== renderExpirationTime
              ? ((fn = getUnmaskedContext(workInProgress)),
                (fn = getMaskedContext(workInProgress, fn)),
                (props = props(renderExpirationTime, fn)),
                (workInProgress.effectTag |= 1),
                reconcileChildren(current, workInProgress, props),
                (workInProgress.memoizedProps = renderExpirationTime),
                (current = workInProgress.child))
              : (current = bailoutOnAlreadyFinishedWork(
                  current,
                  workInProgress
                )),
            current
          );
        case 2:
          props = pushLegacyContextProvider(workInProgress);
          null === current
            ? null === workInProgress.stateNode
              ? (constructClassInstance(
                  workInProgress,
                  workInProgress.pendingProps
                ),
                mountClassInstance(workInProgress, renderExpirationTime),
                (fn = !0))
              : (fn = resumeMountClassInstance(
                  workInProgress,
                  renderExpirationTime
                ))
            : (fn = updateClassInstance(
                current,
                workInProgress,
                renderExpirationTime
              ));
          unmaskedContext = !1;
          var updateQueue = workInProgress.updateQueue;
          null !== updateQueue &&
            null !== updateQueue.capturedValues &&
            (unmaskedContext = fn = !0);
          return finishClassComponent(
            current,
            workInProgress,
            fn,
            props,
            unmaskedContext,
            renderExpirationTime
          );
        case 3:
          a: if (
            (pushHostRootContext(workInProgress),
            (fn = workInProgress.updateQueue),
            null !== fn)
          ) {
            unmaskedContext = workInProgress.memoizedState;
            props = processUpdateQueue(
              current,
              workInProgress,
              fn,
              null,
              null,
              renderExpirationTime
            );
            workInProgress.memoizedState = props;
            fn = workInProgress.updateQueue;
            if (null !== fn && null !== fn.capturedValues) fn = null;
            else if (unmaskedContext === props) {
              resetHydrationState();
              current = bailoutOnAlreadyFinishedWork(current, workInProgress);
              break a;
            } else fn = props.element;
            unmaskedContext = workInProgress.stateNode;
            (null === current || null === current.child) &&
            unmaskedContext.hydrate &&
            enterHydrationState(workInProgress)
              ? ((workInProgress.effectTag |= 2),
                (workInProgress.child = mountChildFibers(
                  workInProgress,
                  null,
                  fn,
                  renderExpirationTime
                )))
              : (resetHydrationState(),
                reconcileChildren(current, workInProgress, fn));
            workInProgress.memoizedState = props;
            current = workInProgress.child;
          } else
            resetHydrationState(),
              (current = bailoutOnAlreadyFinishedWork(current, workInProgress));
          return current;
        case 5:
          a: {
            pushHostContext(workInProgress);
            null === current &&
              tryToClaimNextHydratableInstance(workInProgress);
            props = workInProgress.type;
            updateQueue = workInProgress.memoizedProps;
            fn = workInProgress.pendingProps;
            unmaskedContext = null !== current ? current.memoizedProps : null;
            if (!hasLegacyContextChanged() && updateQueue === fn) {
              if (
                (updateQueue =
                  workInProgress.mode & 1 &&
                  shouldDeprioritizeSubtree(props, fn))
              )
                workInProgress.expirationTime = 1073741823;
              if (!updateQueue || 1073741823 !== renderExpirationTime) {
                current = bailoutOnAlreadyFinishedWork(current, workInProgress);
                break a;
              }
            }
            updateQueue = fn.children;
            shouldSetTextContent(props, fn)
              ? (updateQueue = null)
              : unmaskedContext &&
                shouldSetTextContent(props, unmaskedContext) &&
                (workInProgress.effectTag |= 16);
            markRef(current, workInProgress);
            1073741823 !== renderExpirationTime &&
            workInProgress.mode & 1 &&
            shouldDeprioritizeSubtree(props, fn)
              ? ((workInProgress.expirationTime = 1073741823),
                (workInProgress.memoizedProps = fn),
                (current = null))
              : (reconcileChildren(current, workInProgress, updateQueue),
                (workInProgress.memoizedProps = fn),
                (current = workInProgress.child));
          }
          return current;
        case 6:
          return (
            null === current &&
              tryToClaimNextHydratableInstance(workInProgress),
            (workInProgress.memoizedProps = workInProgress.pendingProps),
            null
          );
        case 8:
          workInProgress.tag = 7;
        case 7:
          return (
            (props = workInProgress.pendingProps),
            hasLegacyContextChanged() ||
              workInProgress.memoizedProps !== props ||
              (props = workInProgress.memoizedProps),
            (fn = props.children),
            (workInProgress.stateNode =
              null === current
                ? mountChildFibers(
                    workInProgress,
                    workInProgress.stateNode,
                    fn,
                    renderExpirationTime
                  )
                : reconcileChildFibers(
                    workInProgress,
                    current.stateNode,
                    fn,
                    renderExpirationTime
                  )),
            (workInProgress.memoizedProps = props),
            workInProgress.stateNode
          );
        case 9:
          return null;
        case 4:
          return (
            pushHostContainer(
              workInProgress,
              workInProgress.stateNode.containerInfo
            ),
            (props = workInProgress.pendingProps),
            hasLegacyContextChanged() || workInProgress.memoizedProps !== props
              ? (null === current
                  ? (workInProgress.child = reconcileChildFibers(
                      workInProgress,
                      null,
                      props,
                      renderExpirationTime
                    ))
                  : reconcileChildren(current, workInProgress, props),
                (workInProgress.memoizedProps = props),
                (current = workInProgress.child))
              : (current = bailoutOnAlreadyFinishedWork(
                  current,
                  workInProgress
                )),
            current
          );
        case 14:
          return (
            (renderExpirationTime = workInProgress.type.render),
            (renderExpirationTime = renderExpirationTime(
              workInProgress.pendingProps,
              workInProgress.ref
            )),
            reconcileChildren(current, workInProgress, renderExpirationTime),
            (workInProgress.memoizedProps = renderExpirationTime),
            workInProgress.child
          );
        case 10:
          return (
            (renderExpirationTime = workInProgress.pendingProps),
            hasLegacyContextChanged() ||
            workInProgress.memoizedProps !== renderExpirationTime
              ? (reconcileChildren(
                  current,
                  workInProgress,
                  renderExpirationTime
                ),
                (workInProgress.memoizedProps = renderExpirationTime),
                (current = workInProgress.child))
              : (current = bailoutOnAlreadyFinishedWork(
                  current,
                  workInProgress
                )),
            current
          );
        case 11:
          return (
            (renderExpirationTime = workInProgress.pendingProps.children),
            hasLegacyContextChanged() ||
            (null !== renderExpirationTime &&
              workInProgress.memoizedProps !== renderExpirationTime)
              ? (reconcileChildren(
                  current,
                  workInProgress,
                  renderExpirationTime
                ),
                (workInProgress.memoizedProps = renderExpirationTime),
                (current = workInProgress.child))
              : (current = bailoutOnAlreadyFinishedWork(
                  current,
                  workInProgress
                )),
            current
          );
        case 13:
          return updateContextProvider(
            current,
            workInProgress,
            renderExpirationTime
          );
        case 12:
          a: {
            fn = workInProgress.type;
            unmaskedContext = workInProgress.pendingProps;
            updateQueue = workInProgress.memoizedProps;
            props = fn._currentValue;
            var changedBits = fn._changedBits;
            if (
              hasLegacyContextChanged() ||
              0 !== changedBits ||
              updateQueue !== unmaskedContext
            ) {
              workInProgress.memoizedProps = unmaskedContext;
              var observedBits = unmaskedContext.unstable_observedBits;
              if (void 0 === observedBits || null === observedBits)
                observedBits = 1073741823;
              workInProgress.stateNode = observedBits;
              if (0 !== (changedBits & observedBits))
                propagateContextChange(
                  workInProgress,
                  fn,
                  changedBits,
                  renderExpirationTime
                );
              else if (updateQueue === unmaskedContext) {
                current = bailoutOnAlreadyFinishedWork(current, workInProgress);
                break a;
              }
              renderExpirationTime = unmaskedContext.children;
              renderExpirationTime = renderExpirationTime(props);
              reconcileChildren(current, workInProgress, renderExpirationTime);
              current = workInProgress.child;
            } else
              current = bailoutOnAlreadyFinishedWork(current, workInProgress);
          }
          return current;
        default:
          invariant(
            !1,
            "Unknown unit of work tag. This error is likely caused by a bug in React. Please file an issue."
          );
      }
    }
  };
