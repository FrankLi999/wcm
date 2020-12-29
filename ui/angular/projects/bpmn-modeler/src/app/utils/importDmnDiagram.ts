import { Observable } from "rxjs";

/**
 * An operator that imports the first XML piped via the piped diagram XML
 * into the passed DmnJS instance.
 */
export const importDmnDiagram = (dmnJS) => <Object>(
  source: Observable<string>
) =>
  new Observable<string>((observer) => {
    const subscription = source.subscribe({
      next(xml: string) {
        // canceling the subscription as we are interested
        // in the first diagram to display only
        subscription.unsubscribe();

        dmnJS.importXML(xml, (err, warnings) => {
          if (err) {
            observer.error(err);
          } else {
            observer.next(warnings);
          }
          observer.complete();
        });
      },
      error(e) {
        console.log("ERROR");
        observer.error(e);
      },
      complete() {
        observer.complete();
      },
    });
  });
