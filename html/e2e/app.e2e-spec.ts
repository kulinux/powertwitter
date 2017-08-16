import { HtmlPage } from './app.po';

describe('html App', () => {
  let page: HtmlPage;

  beforeEach(() => {
    page = new HtmlPage();
  });

  it('should display welcome message', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('Welcome to app!');
  });
});
