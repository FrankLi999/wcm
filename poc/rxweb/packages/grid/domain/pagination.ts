import { Item } from "@rxweb/dom";
import { Collection } from './collection'
import { TemplateConfig } from "../interface/config/template-config";
import { FooterDesignClass } from './footer-design-class';
import { EVENTS } from "../const/events.const";


export class Pagination extends Collection {

    private _footerTemplate: TemplateConfig;

    footerDesignClass: FooterDesignClass;

    private _maxPerPage: number = 5;

    maxNavigationPage: number = 5;

    pagingSource: number[] = [5, 10, 100, 500, 1000, 50000];

    paginationConfigs: Item[] = new Array<Item>();

    currentPage: number;

    private startCount: number = 1;

    private endCount: number = this.maxPerPage;

    private _sourceLength: number;

    private set sourceLength(value: number) {
        this._sourceLength = value;
    }

    private get sourceLength() {
        return this._sourceLength;
    }

    private get numberOfPages(): number {
        return Math.ceil(this.bindingSource.length / this.maxPerPage)
    }

    constructor(source: any[], model: Function) {
        super(source, model);
        this.footerDesignClass = new FooterDesignClass();
        this.currentPage = 1;
    }

    set maxPerPage(value: number) {
        this._maxPerPage = value;
        this.updateStartEndCount();
    }

    get maxPerPage() {
        return this._maxPerPage;
    }

    next() {
        if ((this.currentPage + 1) < this.numberOfPages)
            this.goTo(this.currentPage + 1);
    }

    previous() {
        if ((this.currentPage + 1) < this.numberOfPages)
            this.goTo(this.currentPage - 1);
    }

    goTo(pageNumber: number) {
        this.currentPage = pageNumber;
    }

    pagination() {
        var pages = this.getPages();
        pages.forEach(i => {
            this.paginationConfigs.push(new Item({ text: i, disabled: true, active: this.currentPage == i }, ["text", "disabled", "active"]))
        })
        this.updateStartEndCount();
    }

    private getPages() {
        if (this.currentPage < 1)
            this.currentPage = 1;
        else if (this.currentPage > this.numberOfPages)
            this.currentPage = this.numberOfPages;

        let startPage: number, endPage: number;
        if (this.numberOfPages <= this.maxNavigationPage) {
            startPage = 1;
            endPage = this.numberOfPages;
        } else {
            let maxPagesBeforeCurrentPage = Math.floor(this.maxNavigationPage / 2);
            let maxPagesAfterCurrentPage = Math.ceil(this.maxNavigationPage / 2) - 1;
            if (this.currentPage <= maxPagesBeforeCurrentPage) {
                startPage = 1;
                endPage = this.maxNavigationPage;
            } else if (this.currentPage + maxPagesAfterCurrentPage >= this.numberOfPages) {
                startPage = this.numberOfPages - this.maxNavigationPage + 1;
                endPage = this.numberOfPages;
            } else {
                startPage = this.currentPage - maxPagesBeforeCurrentPage;
                endPage = this.currentPage + maxPagesAfterCurrentPage;
            }
        }
        let startIndex = (this.currentPage - 1) * this.maxPerPage;
        let endIndex = Math.min(startIndex + this.maxPerPage - 1, this.length - 1);

        return Array.from(Array((endPage + 1) - startPage).keys()).map(i => startPage + i);
    }

    get footerTemplate() {
        return this._footerTemplate;
    }

    set footerTemplate(value: TemplateConfig) {
        this._footerTemplate = value;
    }

    protected onMaxPerPageChanging(element: Event) {
        this.maxPerPage = parseInt((<HTMLSelectElement>element.currentTarget).value);
        this.currentPage = 1;
        let source = this.take(this.bindingSource, this.maxPerPage);
        this.mapWithModel(source);
        this.updatePagination();
        this.updateStartEndCount();
    }

    protected onPageChanging(element: Event) {
        this.currentPage = parseInt((<HTMLAnchorElement>element.target).innerText);
        this.changeSource();
        this.updatePagination();
        this.updateStartEndCount();
    }



    protected updatePagination() {
        var pageConfigLength = this.paginationConfigs.length;
        var pages = this.getPages();
        for (var i = 0, j = pages.length; i < j; i++) {
            if (pageConfigLength > i)
                this.paginationConfigs[i].setValue({ text: pages[i], active: this.currentPage == pages[i] });
            else {
                var row = new Item({ text: pages[i], disabled: true }, ["text", "disabled", "active"]);
                this.paginationConfigs.push(row);
                this.eventSubscriber.dispatch(EVENTS.ADD_ROWS, { row: row, index: i, identity: "pagination" });
            }
        }
        if (pages.length < this.paginationConfigs.length)
            this.removeItem(this.paginationConfigs, pages.length, this.paginationConfigs.length, "list-item")
    }

    remove(id: number) {
        var item = this.bindingSource.filter(t => t[this.primaryKey] == id)[0];
        var indexOf = this.bindingSource.indexOf(item);
        var sourceItem = this.source.filter(t => t[this.primaryKey] == id)[0];
        var itemIndexOf = this.source.indexOf(sourceItem);
        if (indexOf != -1 && itemIndexOf != -1) {
            this.source.splice(itemIndexOf, 1);
            this.bindingSource.splice(indexOf, 1);
            this.changeSource();
            this.updatePagination();
            this.updateStartEndCount();
        }
    }

    protected changeSource() {
        var source = [];
        if (this.currentPage > 1)
            source = this.skip(this.bindingSource, Math.max(0, (this.currentPage - 1) * this.maxPerPage));
        source = this.take(source.length > 0 ? source : this.bindingSource, Math.max(0, this.maxPerPage));
        this.mapWithModel(source);
    }

    protected updateStartEndCount() {
        var endCount = 0;
        if (this.currentPage == 1) {
            this.startCount = 1;
            endCount = this.maxPerPage > this.length ? this.length : this.maxPerPage;
        } else {
            this.startCount = (this.maxPerPage * ((this.currentPage - 1))) + 1
            if (this.startCount < 0)
                this.startCount = 0;
            endCount = this.maxPerPage * this.currentPage;
            endCount = endCount > this.length ? this.length : endCount;
        }
        this.endCount = endCount;
        this.sourceLength = this.bindingSource.length;
    }
}